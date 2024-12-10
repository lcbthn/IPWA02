package ipwa02;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Testlaeufe extends Aufgaben
{
    @OneToMany (mappedBy = "testlauf")
    private List<Testfaelle> testfaelle = new ArrayList<Testfaelle>();

    public List<Testfaelle> getTestfaelle()
    {
        return testfaelle;
    }

    public void setTestfaelle(List<Testfaelle> testfaelle)
    {
        this.testfaelle = testfaelle;
    }

    @ManyToOne
    @JoinColumn(name = "tester_id")
    private Personen tester;

    public Personen getTester()
    {
        return tester;
    }

    public void setTester(Personen tester)
    {
        this.tester = tester;
    }
}
