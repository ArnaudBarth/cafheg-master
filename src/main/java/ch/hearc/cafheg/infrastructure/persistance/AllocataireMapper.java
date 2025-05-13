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
    logger.debug("findAll() avec filtre '{}'", likeNom);
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement preparedStatement;
      if (likeNom == null) {
        logger.debug("SQL: {}", QUERY_FIND_ALL);
        preparedStatement = connection.prepareStatement(QUERY_FIND_ALL);
      } else {
        logger.debug("SQL: {}", QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement = connection.prepareStatement(QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement.setString(1, likeNom + "%");
      }

      List<Allocataire> allocataires = new ArrayList<>();

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          logger.debug("Mapping d'un allocataire du ResultSet");
          allocataires.add(new Allocataire(
                  new NoAVS(resultSet.getString(3)),
                  resultSet.getString(2),
                  resultSet.getString(1)));
        }
      }

      logger.debug("Allocataires trouvés {}", allocataires.size());
      return allocataires;
    } catch (SQLException e) {
      logger.error("Erreur lors de la récupération des allocataires", e);
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

      if(resultSet.next()) {
        logger.debug("Allocataire trouvé, mapping en cours");
        return new Allocataire(
                new NoAVS(resultSet.getString(1)),
                resultSet.getString(2),
                resultSet.getString(3));
      } else {
        logger.warn("Aucun allocataire trouvé avec l'ID {}", id);
        return null;
      }
    } catch (SQLException e) {
      logger.error("Erreur lors de la recherche de l'allocataire par ID {}", id, e);
      throw new RuntimeException(e);
    }
  }
  public void supprimerAllocataireParId(Long allocataireId) {
    logger.debug("Suppression de l'allocataire {}", allocataireId);
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_DELETE_ALLOCATAIRE);
      preparedStatement.setLong(1, allocataireId);

      int rows = preparedStatement.executeUpdate();
      if (rows == 0) {
        logger.warn("Suppression échouée : aucun allocataire avec l'ID {}", allocataireId);
      } else {
        logger.info("Allocataire {} supprimé", allocataireId);
      }
    } catch (SQLException e) {
      logger.error("Erreur lors de la suppression de l'allocataire {}", allocataireId, e);
      throw new RuntimeException(e);
    }
  }

  public void modifierNomPrenom(Long numero, String nom, String prenom) {
    logger.debug("Modification du nom et prénom de l'allocataire {}", numero);
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement ps = connection.prepareStatement(QUERY_UPDATE_ALLOCATAIRE);
      ps.setString(1, nom);
      ps.setString(2, prenom);
      ps.setLong(3, numero); // On utilise bien la PK réelle

      int rows = ps.executeUpdate();
      if (rows == 0) {
        logger.warn("Aucune ligne modifiée pour l'allocataire {}", numero);
      } else {
        logger.info("Allocataire {} mis à jour avec succès", numero);
      }
    } catch (SQLException e) {
      logger.error("Erreur lors de la modification de l'allocataire {}", numero, e);
      throw new RuntimeException(e);
    }
  }

  public Allocataire findByNumero(Long numero) {
    logger.debug("Recherche de l'allocataire par numéro {}", numero);
    Connection connection = activeJDBCConnection();

    try {
      PreparedStatement ps = connection.prepareStatement(QUERY_FIND_ALLOCATAIRE_BY_NUMERO);
      ps.setLong(1, numero);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        logger.debug("Allocataire trouvé, mapping...");
        return new Allocataire(
                new NoAVS(rs.getString("NO_AVS")),
                rs.getString("NOM"),
                rs.getString("PRENOM")
        );
      } else {
        logger.warn("Aucun allocataire trouvé avec le numéro {}", numero);
        return null;
      }
    } catch (SQLException e) {
      logger.error("Erreur lors de la recherche par numéro {}", numero, e);
      throw new RuntimeException(e);
    }
  }
}