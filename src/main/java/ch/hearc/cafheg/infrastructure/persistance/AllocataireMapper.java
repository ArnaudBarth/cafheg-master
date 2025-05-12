package ch.hearc.cafheg.infrastructure.persistance;

import ch.hearc.cafheg.business.allocations.Allocataire;
import ch.hearc.cafheg.business.allocations.NoAVS;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocataireMapper extends Mapper {
  private static final String QUERY_FIND_ALL = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES";
  private static final String QUERY_FIND_WHERE_NOM_LIKE = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES WHERE NOM LIKE ?";
  private static final String QUERY_FIND_WHERE_NUMERO = "SELECT NO_AVS, NOM, PRENOM FROM ALLOCATAIRES WHERE NUMERO=?";
  private final String QUERY_DELETE_ALLOCATAIRE = "DELETE FROM ALLOCATAIRES WHERE NUMERO = ?";
  private final String QUERY_UPDATE_ALLOCATAIRE = "UPDATE ALLOCATAIRES SET NOM = ?, PRENOM = ? WHERE NUMERO = ?";
  private final String QUERY_FIND_ALLOCATAIRE_BY_NUMERO = "SELECT NUMERO, NO_AVS, NOM, PRENOM FROM ALLOCATAIRES WHERE NUMERO = ?";
  private static final Logger logger = LoggerFactory.getLogger(AllocataireMapper.class);

  public List<Allocataire> findAll(String likeNom) {
    logger.debug("findAll() {}", likeNom);
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement preparedStatement;

      if (likeNom == null) {
        logger.debug("SQL: {}", QUERY_FIND_ALL);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_ALL);
      } else {
        logger.debug("SQL: {}", QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement.setString(1, likeNom + "%");
      }

      logger.debug("Allocation d'un nouveau tableau");
      List<Allocataire> allocataires = new ArrayList<>();

      logger.debug("Exécution de la requête");
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        logger.debug("Allocataire mapping");

        while (resultSet.next()) {
          logger.debug("ResultSet#next");
          allocataires
              .add(new Allocataire(new NoAVS(resultSet.getString(3)), resultSet.getString(2),
                  resultSet.getString(1)));
        }
      }

      logger.debug("Allocataires trouvés {}", allocataires.size());
      return allocataires;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Allocataire findById(long id) {
    logger.debug("findById() {}", id);
    Connection connection = activeJDBCConnection();

    try {
      logger.debug("SQL: {}", QUERY_FIND_WHERE_NUMERO);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_WHERE_NUMERO);
      preparedStatement.setLong(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      logger.debug("ResultSet#next");
      resultSet.next();
      logger.debug("Allocataire mapping");
      return new Allocataire(new NoAVS(resultSet.getString(1)),
          resultSet.getString(2), resultSet.getString(3));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public void supprimerAllocataireParId(Long allocataireId) {
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_DELETE_ALLOCATAIRE);
      preparedStatement.setLong(1, allocataireId);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public void modifierNomPrenom(Long numero, String nom, String prenom) {
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement ps = connection.prepareStatement(QUERY_UPDATE_ALLOCATAIRE);
      ps.setString(1, nom);
      ps.setString(2, prenom);
      ps.setLong(3, numero); // On utilise bien la PK réelle
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Allocataire findByNumero(Long numero) {
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement ps = connection.prepareStatement(QUERY_FIND_ALLOCATAIRE_BY_NUMERO);
      ps.setLong(1, numero);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return new Allocataire(
                new NoAVS(rs.getString("NO_AVS")),
                rs.getString("NOM"),
                rs.getString("PRENOM")
        );
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}