package com.humdet;

public class Conf {
    private String shared_pref_name = "settings_gz";
    //private String domen = "http://18.206.137.118:8080/api/";
    private String domen = "https://lesa01.herokuapp.com/api/";
//<!--android:networkSecurityConfig="@xml/network_security_config"-->

    private final String LANG = "lang";
    private final int RU = 1;
    private final int EN = 2;
    private final int AR = 3;



    public String getShared_pref_name() {
        return shared_pref_name;
    }

    public void setShared_pref_name(String shared_pref_name) {
        this.shared_pref_name = shared_pref_name;
    }

    public String getLANG() {
        return LANG;
    }

    public int getRU() {
        return RU;
    }

    public int getEN() {
        return EN;
    }

    public int getAR() {
        return AR;
    }

    public String getDomen() {
        return domen;
    }

    public void setDomen(String domen) {
        this.domen = domen;
    }
}
