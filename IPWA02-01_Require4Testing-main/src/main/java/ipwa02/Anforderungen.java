package ipwa02;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Anforderungen  extends Aufgaben
{

    @OneToMany(mappedBy = "anforderung")
    private List<Testfaelle> testfall;

    public List<Testfaelle> getTestfall()
    {
        return testfall;
    }

    public void setTestfall(List<Testfaelle> testfall)
    {
        this.testfall = testfall;
    }
}
