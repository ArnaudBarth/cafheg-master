import ch.hearc.cafheg.business.allocations.AllocationService;
import ch.hearc.cafheg.infrastructure.persistance.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistance.Database;
import ch.hearc.cafheg.infrastructure.persistance.VersementMapper;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

public class MyTestsIT {

    private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private AllocationService allocationService;

    static class FakeVersementMapper extends VersementMapper {
        @Override
        public boolean hasVersementsForAllocataire(Long numero) {
            return false;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Database.setTestConnection(conn);

        // Nettoyer la table si elle existe
        conn.createStatement().executeUpdate("DROP TABLE IF EXISTS ALLOCATAIRES");

        // Créer la table ALLOCATAIRES
        conn.createStatement().executeUpdate(
                "CREATE TABLE ALLOCATAIRES (" +
                        "NUMERO INT PRIMARY KEY, " +
                        "NO_AVS VARCHAR(200), " +
                        "PRENOM VARCHAR(200), " +
                        "NOM VARCHAR(200))"
        );

        AllocataireMapper allocataireMapper = new AllocataireMapper();
        VersementMapper fakeVersementMapper = new FakeVersementMapper();
        allocationService = new AllocationService(allocataireMapper, null, fakeVersementMapper);

        IDatabaseTester databaseTester = new JdbcDatabaseTester("org.h2.Driver", JDBC_URL, USER, PASSWORD);
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        File file = new File(getClass().getClassLoader().getResource("dataset.xml").getFile());
        databaseTester.setDataSet(builder.build(file));
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    void testSuppressionAllocataire() throws Exception {
        allocationService.supprimerAllocataireSiAucunVersement(1L);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM ALLOCATAIRES WHERE NUMERO = ?");
            check.setInt(1, 1);
            ResultSet rs = check.executeQuery();
            rs.next();

            int count = rs.getInt(1);
            assertThat(count).isZero();
        }
    }

    @Test
    void testModificationAllocataire() throws Exception {
        allocationService.modifierAllocataire(1L, "Dupont", "Pierre");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            PreparedStatement check = conn.prepareStatement("SELECT PRENOM FROM ALLOCATAIRES WHERE NUMERO = ?");
            check.setInt(1, 1);
            ResultSet rs = check.executeQuery();
            rs.next();

            String prenom = rs.getString("PRENOM");
            assertThat(prenom).isEqualTo("Pierre");
        }
    }
}
