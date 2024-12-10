package ipwa02;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.persistence.*;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

import java.io.Serializable;
import java.util.*;

@Named("Service")
@SessionScoped
public class Service implements Serializable
{
    /*******************************************************************************************************************
    Attribute und Methoden für Generelle Aufgaben
     *******************************************************************************************************************/
    //region Generell
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IPWA02_DB");
    private EntityManager em = emf.createEntityManager();

    private List<String> einAusgabeListe;
    public List<String> getEinAusgabeListe() {
        return einAusgabeListe;
    }
    public void setEinAusgabeListe(List<String> einAusgabeListe) {
        this.einAusgabeListe = einAusgabeListe;
    }
    public void clearEinAusgabeListe()
    {
        for (int i = 0; i < 10; i++)
        {
            einAusgabeListe.set(i, "");
        }
    }
    //endregion

    /*******************************************************************************************************************
    Userverwaltung
     *******************************************************************************************************************/
    //region Userverwaltung
    private Personen angemeldetePerson = null;

    public Personen getAngemeldetePerson()
    {
        return angemeldetePerson;
    }
    public void setAngemeldetePerson(Personen angemeldetePerson)
    {
        this.angemeldetePerson = angemeldetePerson;
    }
    public boolean isLoggedIn()
    {
        return angemeldetePerson != null;
    }
    public String anmelden()
    {
        try
        {
            angemeldetePerson = em.createQuery("SELECT p FROM Personen p WHERE p.username = :username", Personen.class)
                    .setParameter("username", einAusgabeListe.get(0))
                    .getSingleResult();
            if (!Objects.equals(angemeldetePerson.getPassword(), einAusgabeListe.get(1)))
            {
                angemeldetePerson = null;
            }
        }
        catch (Exception e) {
            clearEinAusgabeListe();
            return "index.xhtml?faces-redirect=true";
        }
        clearEinAusgabeListe();
        return "index.xhtml?faces-redirect=true";
    }
    public String abmelden()
    {
        angemeldetePerson = null;
        clearEinAusgabeListe();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "LoginRegister.xhtml?faces-redirect=true";
    }
    public String registrieren()
    {
        if (Objects.equals(einAusgabeListe.get(1), einAusgabeListe.get(2)))  //Passwort übereinstimmung
        {
            try  //Suche ob Benutzername vergeben ist
            {
                angemeldetePerson = em.find(Personen.class, einAusgabeListe.get(0));
            }
            catch (Exception e) {}
            if (angemeldetePerson == null) //Benutzer anlegen
            {
                angemeldetePerson = new Personen();
                angemeldetePerson.setUsername(einAusgabeListe.get(0));
                angemeldetePerson.setPassword(einAusgabeListe.get(1));
                angemeldetePerson.setTeam(einAusgabeListe.get(3));
                angemeldetePerson.setRole(einAusgabeListe.get(4));
                try { //Benutzer speichern
                    em.getTransaction().begin();
                    em.persist(angemeldetePerson);
                    em.getTransaction().commit();
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback(); // Rollback bei Fehler
                    }
                    angemeldetePerson = null;
                }
            }
        }
        clearEinAusgabeListe();
        return "index.xhtml?faces-redirect=true";
    }
    public String getRolle()
    {
        if (angemeldetePerson == null) return "";
        return angemeldetePerson.getRole();
    }
    //endregion

    /*******************************************************************************************************************
    Speicher / Bearbeiten Anforderung
     *******************************************************************************************************************/
    //region Anforderung
    public String AnforderungSpeichern()
    {
        Anforderungen anforderung = null;
        if (!isLoggedIn() && einAusgabeListe.get(0) != "") {return "index.xhtml?faces-redirect=true";} //Ohne Titel nicht Speicherbar
        try //Testen ob die Aufgabe schon existiert und überschrieben werden muss
        {
            anforderung = em.createQuery("SELECT p FROM Anforderungen p WHERE p.titel = :titel AND p.team = :team", Anforderungen.class)
                    .setParameter("titel", einAusgabeListe.get(0))
                    .setParameter("team", einAusgabeListe.get(1))
                    .getSingleResult();
        }catch (Exception e) {}
        if (anforderung == null) {anforderung = new Anforderungen();} //wenn nicht existiert, dann neu erzeugen
        anforderung.setTitel(einAusgabeListe.get(0));
        anforderung.setBeschreibung(einAusgabeListe.get(1));
        anforderung.setTeam(angemeldetePerson.getTeam());
        anforderung.setErsteller(angemeldetePerson);
        try { //Aufgabe speichern
            em.getTransaction().begin();
            em.persist(anforderung);
            em.getTransaction().commit();
            aufgabenListeAdd(anforderung);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // Rollback bei Fehler
            }
        }
        clearEinAusgabeListe();
        return "index.xhtml?faces-redirect=true";
    }
    //endregion

    /*******************************************************************************************************************
    Speicher / Bearbeiten Testfall
     *******************************************************************************************************************/
    //region Testfall
    private List<SelectItem> stringAnforderungsTitelListe = null;
    public List<SelectItem> getStringAnforderungsTitelListe()
    {
        if (stringAnforderungsTitelListe == null)
        {
            initStringAnforderungsTitelListe();
        }
        return stringAnforderungsTitelListe;
    }
    public void setStringAnforderungsTitelListe(List<SelectItem> stringAnforderungsTitelListe)
    {
        this.stringAnforderungsTitelListe = stringAnforderungsTitelListe;
    }
    public void initStringAnforderungsTitelListe()
    {
        stringAnforderungsTitelListe = new ArrayList<>();
        for (Anforderungen a : getAnforderungsListe())
        {
            stringAnforderungsTitelListe.add(new SelectItem(a.getTitel()));
        }
    }
    public String testfallSpeichern()
    {
        Testfaelle testfall = null;
        if (!isLoggedIn() && einAusgabeListe.get(0) != "") {return "index.xhtml?faces-redirect=true";} //Ohne Titel nicht Speicherbar
        try //Testen ob die Aufgabe schon existiert und überschrieben werden muss
        {
            testfall = em.createQuery("SELECT p FROM Testfaelle p WHERE p.titel = :titel AND p.team = :team", Testfaelle.class)
                    .setParameter("titel", einAusgabeListe.get(0))
                    .setParameter("team", angemeldetePerson.getTeam())
                    .getSingleResult();
        }catch (Exception e) {}
        //wenn nicht existiert, dann neu erzeugen
        if (testfall == null) {
            testfall = new Testfaelle();
            testfall.setTitel(einAusgabeListe.get(0));
            testfall.setBeschreibung(einAusgabeListe.get(1));
            testfall.setTeam(angemeldetePerson.getTeam());
            testfall.setErsteller(angemeldetePerson);
            for (Anforderungen a : getAnforderungsListe())
            {
                if (Objects.equals(a.getTitel(), einAusgabeListe.get(2)))
                {
                    testfall.setAnforderung(a);
                    break;
                }
            }
        }
        //muss immer geupdatet werden
        testfall.setStatusErgebnis(einAusgabeListe.get(3));
        testfall.setTestschritte(einAusgabeListe.get(4));
        try { //Aufgabe speichern
            em.getTransaction().begin();
            em.persist(testfall);
            em.persist(testfall.getAnforderung());
            em.getTransaction().commit();
            aufgabenListeAdd(testfall);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // Rollback bei Fehler
            }
        }
        initAufgabenListe();
        clearEinAusgabeListe();
        return "index.xhtml?faces-redirect=true";
    }
    //endregion

    /*******************************************************************************************************************
    Speicher / Bearbeiten Testlauf
     *******************************************************************************************************************/
    //region Testlauf
    private List<Personen> testerListe = null;
    public List<Personen> getTesterListe()
    {
        if (testerListe != null) {return testerListe;}
        testerListe = em.createQuery("SELECT p FROM Personen p WHERE p.role = :rolle AND p.team = :team", Personen.class)
                .setParameter("rolle", "Tester")
                .setParameter("team", angemeldetePerson.getTeam())
                .getResultList();
        return testerListe;
    }
    private DualListModel<String> testfallpicker = null;
    public DualListModel<String> getTestfallpicker() {
        if (testfallpicker == null) {initTestfallpicker();}
        return testfallpicker;
    }
    public void setTestfallpicker(DualListModel<String> testfallpicker)
    {
        this.testfallpicker = testfallpicker;
    }
    public void initTestfallpicker()
    {
        List<Testfaelle> source = em.createQuery("SELECT a FROM Testfaelle a WHERE a.team = :team", Testfaelle.class)
                .setParameter("team", angemeldetePerson.getTeam())
                .getResultList();
        List<String> sourceString = new ArrayList<>();
        for (Testfaelle testfaelle : source) {
            sourceString.add(testfaelle.getTitel());
        }
        testfallpicker = new DualListModel<>(sourceString,new ArrayList<>());
    }
    private List<SelectItem> stringtesterNamensListe = null;
    public List<SelectItem> getStringtesterNamensListe()
    {
        if (stringtesterNamensListe == null) {initStringtesterNamensListe();}
        return stringtesterNamensListe;
    }
    public void setStringtesterNamensListe(List<SelectItem> stringtesterNamensListe)
    {
        this.stringtesterNamensListe = stringtesterNamensListe;
    }
    public void initStringtesterNamensListe()
    {
        stringtesterNamensListe = new ArrayList<>();
        stringtesterNamensListe.add(new SelectItem("keiner"));
        for (Personen p : getTesterListe())
        {
            stringtesterNamensListe.add(new SelectItem(p.getUsername()));
        }
    }
    public String TestlaufSpeichern()
    {
        Testlaeufe testlauf = null;
        if (!isLoggedIn() && einAusgabeListe.get(0) != "") {return "index.xhtml?faces-redirect=true";} //Ohne Titel nicht Speicherbar
        try //Testen ob die Aufgabe schon existiert und überschrieben werden muss
        {
            testlauf = em.createQuery("SELECT p FROM Testlaeufe p WHERE p.titel = :titel AND p.team = :team", Testlaeufe.class)
                    .setParameter("titel", einAusgabeListe.get(0))
                    .setParameter("team", einAusgabeListe.get(1))
                    .getSingleResult();
        }catch (Exception e) {}
        if (testlauf == null) {testlauf = new Testlaeufe();} //wenn nicht existiert, dann neu erzeugen
        testlauf.setTitel(einAusgabeListe.get(0));
        testlauf.setBeschreibung(einAusgabeListe.get(1));
        testlauf.setTeam(angemeldetePerson.getTeam());
        testlauf.setErsteller(angemeldetePerson);
        for (Personen p : getTesterListe())
        {
            if (Objects.equals(p.getUsername(), einAusgabeListe.get(2)))
            {
                testlauf.setTester(p);
                break;
            }
        }
        List<Testfaelle> testfallListe = new ArrayList<>();
        for (String s : testfallpicker.getTarget())
        {
            testfallListe.add(em.createQuery("SELECT t FROM Testfaelle t WHERE t.titel = :titel", Testfaelle.class)
                    .setParameter("titel", s)
                    .getSingleResult());
        }
        testlauf.setTestfaelle(testfallListe);
        try { //Aufgabe speichern
            em.getTransaction().begin();
            em.persist(testlauf);
            for (Testfaelle testfaelle : testfallListe)
            {
                testfaelle.setTestlauf(testlauf);
                em.persist(testfaelle);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // Rollback bei Fehler
            }
        }
        initAufgabenListe();
        clearEinAusgabeListe();
        return "index.xhtml?faces-redirect=true";
    }
    //endregion

    /*******************************************************************************************************************
     Daten Anzeigen
     *******************************************************************************************************************/
    //region Daten Laden
    private List<Anforderungen> anforderungsListe = null;
    public List<Anforderungen> getAnforderungsListe()
    {
        if (anforderungsListe == null)
        {
            initAnforderungsListe();
        }
        return anforderungsListe;
    }
    public void setAnforderungsListe(List<Anforderungen> anforderungsListe)
    {
        this.anforderungsListe = anforderungsListe;
    }
    public void initAnforderungsListe()
    {
        anforderungsListe = em.createQuery("SELECT a FROM Anforderungen a WHERE a.team = :team", Anforderungen.class)
                .setParameter("team", angemeldetePerson.getTeam())
                .getResultList();
    }
    private Aufgaben aufgabeZuBearbeiten = null;
    public Aufgaben getAufgabeZuBearbeiten()
    {
        return aufgabeZuBearbeiten;
    }
    public void setAufgabeZuBearbeiten(Aufgaben aufgabeZuBearbeiten)
    {
        this.aufgabeZuBearbeiten = aufgabeZuBearbeiten;
    }
    public Class getAufgabZuBearbeitenTyp()
    {
        return aufgabeZuBearbeiten.getClass();
    }
    private List<Testfaelle> testfallListe = null;
    public List<Testfaelle> getTestfallListe()
    {
        if (testfallListe == null)
        {
            initTestfallListe();
        }
        return testfallListe;
    }
    public void setTestfallListe(List<Testfaelle> testfallListe)
    {
        this.testfallListe = testfallListe;
    }
    public void initTestfallListe()
    {
        testfallListe = em.createQuery("SELECT a FROM Testfaelle a WHERE a.team = :team", Testfaelle.class)
                .setParameter("team", angemeldetePerson.getTeam())
                .getResultList();
    }
    private List<Testlaeufe> testlaufListe = null;
    public List<Testlaeufe> getTestlaufListe()
    {
        if (testlaufListe == null)
        {
            initTestlaufListe();
        }
        return testlaufListe;
    }
    public void setTestlaufListe (List<Testlaeufe> testlaufListe)
    {
        this.testlaufListe = testlaufListe;
    }
    public void initTestlaufListe()
    {
        testlaufListe = em.createQuery("SELECT a FROM Testlaeufe a WHERE a.team = :team", Testlaeufe.class)
            .setParameter("team", angemeldetePerson.getTeam())
            .getResultList();
    }
    //endregion

    //region DataView
    public String anzeigerAnforderung(Aufgaben aufgabe)
    {
        String s = "";
        if (aufgabe instanceof Testfaelle)
        {
            Anforderungen a = ((Testfaelle) aufgabe).getAnforderung();
            if (a != null)
            {
                s += a.getTitel();
            }
        }
        return s;
    }
    public String anzeigerTestlauf(Aufgaben aufgabe)
    {
        String s = "";
        if (aufgabe instanceof Testfaelle)
        {
            s += ((Testfaelle) aufgabe).getTestlauf().getTitel();
        }
        return s;
    }
    public String anzeigerLastUpdate(Aufgaben aufgabe)
    {
        String s = "";
        if (aufgabe instanceof Testfaelle)
        {
            s += ((Testfaelle) aufgabe).getLastUpdate();
        }
        return s;
    }
    public String anzeigerStatus(Aufgaben aufgabe)
    {
        String s = "";
        if (aufgabe instanceof Testfaelle)
        {
            s += ((Testfaelle) aufgabe).getStatusErgebnis();
        }
        return s;
    }
    public String anzeigetTestschritte(Aufgaben aufgabe)
    {
        if (aufgabe instanceof Testfaelle)
        {
            String s = ((Testfaelle) aufgabe).getTestschritte();
            if (s == null) return "";
            return s.replace("\n","<br />");
        }
        return "";
    }
    public String anzeigerTester(Aufgaben aufgabe)
    {
        String s = "";
        if (aufgabe instanceof Testlaeufe)
        {
            Personen p = ((Testlaeufe) aufgabe).getTester();
            if (p != null)
            {
                s += p.getUsername();
            }
        }
        return s;
    }
    public String anzeigerTestfallTitelListe(Aufgaben aufgabe)
    {
        StringBuilder s = new StringBuilder();
        List<Testfaelle> testfallListe = null;
        if (aufgabe instanceof Anforderungen)
        {
            testfallListe = ((Anforderungen) aufgabe).getTestfall();
        }
        if (aufgabe instanceof Testlaeufe)
        {
            testfallListe = ((Testlaeufe) aufgabe).getTestfaelle();
        }
        if (testfallListe != null)
        {
            for (Testfaelle t : testfallListe)
            {
                s.append(t.getTitel());
                s.append("<br />");
            }
        }
        if (s.isEmpty()) return "";
        return s.toString();
    }
    public boolean sollAngezeigtWerden(Class c)
    {
        if (Objects.equals(getRolle(), "Requirements Engineer") && c == Anforderungen.class) return true;
        if (Objects.equals(getRolle(), "Testmanager") && (c == Testlaeufe.class || c == Testfaelle.class)) return true;
        if (Objects.equals(getRolle(), "Testfallersteller") && (c == Anforderungen.class || c == Testfaelle.class)) return true;
        if (Objects.equals(getRolle(), "Tester") && c == Testfaelle.class) return true;
        return false;
    }
    private List<Aufgaben> aufgabenListe;

    public List<Aufgaben> getAufgabenListe() {
        if (aufgabenListe == null) {
            initAufgabenListe();
        }
        return aufgabenListe;
    }

    public void initAufgabenListe() {
        aufgabenListe = em.createQuery("SELECT a FROM Aufgaben a WHERE a.team = :team", Aufgaben.class)
                .setParameter("team", angemeldetePerson.getTeam())
                .getResultList();
    }

    public void aufgabenListeAdd(Aufgaben aufgabe)
    {
        this.aufgabenListe.add(aufgabe);
    }

    public String aufgabeBearbeiten(Aufgaben aufgabe) {
        this.aufgabeZuBearbeiten = aufgabe;
        if (aufgabe instanceof Testfaelle) {
            einAusgabeListe.set(0,aufgabe.getTitel());
            einAusgabeListe.set(1,aufgabe.getBeschreibung());
            einAusgabeListe.set(2,((Testfaelle)aufgabe).getAnforderung().getTitel());
            einAusgabeListe.set(3,((Testfaelle)aufgabe).getStatusErgebnis());
            einAusgabeListe.set(4,((Testfaelle)aufgabe).getTestschritte());
            return "TestfallBearbeiten.xhtml?faces-redirect=true";
        } else {
            return "index.xhtml?faces-redirect=true"; // Oder eine Fehlerseite
        }
    }
    //endregion

    //**************Simulierte Daten werden hier im Konstruktor erstellt (war vor einführung von Datenabnk)*************
    //**************einAusgabeListe muss hier die maximale Anzahl an Felder für String übergaben initieren**************
    public Service()
    {
        einAusgabeListe = new ArrayList<>();
        // Initialisiere die Liste mit der Anzahl der benötigten Elementen
        for (int i = 0; i < 5; i++) einAusgabeListe.add("");
    }
}
