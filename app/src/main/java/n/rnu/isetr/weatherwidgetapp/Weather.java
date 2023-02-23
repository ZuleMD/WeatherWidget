package n.rnu.isetr.weatherwidgetapp;

class Weather {

    private Long date;
    private String timeZone;
    private String temp;
    private String icon;

    public Long getDate() {
        return date;
    }

    public String getTimeZone() {
        return timeZone;
    }


    public String getTemp() {
        return temp;
    }


    public String getIcon() {
        return icon;
    }


    public Weather(Long date, String timeZone, String temp, String icon) {
        this.date = date;
        this.timeZone = timeZone;
        this.temp = temp;
        this.icon = icon;
    }
}