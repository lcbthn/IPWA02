package ipwa02;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
public abstract class Aufgaben
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ersteller_id")
    private Personen ersteller;

    @Column(nullable = false)
    private String team;

    @Column(nullable = false)
    private Timestamp date = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false)
    private String titel;

    @Column
    protected String beschreibung;

    public Aufgaben(){date = new Timestamp(System.currentTimeMillis());}

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public Personen getErsteller()
    {
        return ersteller;
    }

    public String getTeam()
    {
        return team;
    }

    public Timestamp getDate()
    {
        return date;
    }

    public String getTitel()
    {
        return titel;
    }

    public String getBeschreibung()
    {
        return beschreibung;
    }

    public void setErsteller(Personen ersteller)
    {
        this.ersteller = ersteller;
    }

    public void setTeam(String team)
    {
        this.team = team;
    }

    public void setDate()
    {
        this.date = new Timestamp(System.currentTimeMillis());
    }

    public void setTitel(String titel)
    {
        this.titel = titel;
    }

    public void setBeschreibung(String beschreibung)
    {
        this.beschreibung = beschreibung;
    }
}
