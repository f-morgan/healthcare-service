import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalServiceImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class MedicalServiceImplTests {
    PatientInfo patientZero;
    PatientInfoFileRepository patientInfoFileRepository;
    MedicalServiceImpl sut;
    SendAlertServiceImpl sendAlertServiceImpl;

    @BeforeAll
    public static void started() {
        System.out.println("Tests started");
    }

    @BeforeEach
    public void init() {
        System.out.println("Test started");
        BloodPressure normalBloodPressure = new BloodPressure(120, 80);
        HealthInfo normalHealthInfo = new HealthInfo(new BigDecimal(36.6), normalBloodPressure);
        patientZero = new PatientInfo("Zero", "John", "Snow", null, normalHealthInfo);

        patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("Zero"))
                .thenReturn(patientZero);

        sendAlertServiceImpl = Mockito.spy(SendAlertServiceImpl.class);

        sut = new MedicalServiceImpl(patientInfoFileRepository, sendAlertServiceImpl);
    }

    @AfterEach
    public void finished() {
        System.out.println("Test finished");
    }

    @AfterAll
    public static void finishedAll() {
        System.out.println("Tests finished");
    }

    @Test
    public void checkBloodPressureTest() {
        sut.checkBloodPressure("Zero", new BloodPressure(160, 120));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(sendAlertServiceImpl).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: Zero, need help", argumentCaptor.getValue());
    }

    @Test
    public void checkTemperatureTest() {
        sut.checkTemperature("Zero", new BigDecimal(38.6));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(sendAlertServiceImpl).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: Zero, need help", argumentCaptor.getValue());
    }

    @Test
    public void checkBloodPressureNormalTest() {
        sut.checkBloodPressure("Zero", new BloodPressure(120, 80));

        Mockito.verify(sendAlertServiceImpl, Mockito.never()).send("Warning, patient with id: Zero, need help");
    }

    @Test
    public void checkTemperatureNormalTest() {
        sut.checkTemperature("Zero", new BigDecimal(35.1));

        Mockito.verify(sendAlertServiceImpl, Mockito.never()).send("Warning, patient with id: Zero, need help");
    }
}
