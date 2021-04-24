package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

public class Movie {
    private String title;
    private String year;
    private String imdbID;
    private String type;
    private String poster;

    public Movie(String title, String year, String imdbID, String type, String poster){
        this.title = title;
        this.year = year;
        this.imdbID = imdbID;
        this.type = type;
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public String getPoster() {
        return poster;
    }

    public String getType() {
        return type;
    }
}
