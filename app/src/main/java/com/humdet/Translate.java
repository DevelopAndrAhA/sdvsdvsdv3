package com.humdet;

public class Translate {



    public String retranStr(String temp){
        temp = temp.toUpperCase();
        String str = "";
        String mass [] = temp.split(" ");
        int lng = mass[0].length();
        int lng2 = mass[1].length();
        for (int i =0; i<= temp.length(); i++){
            try{


                if(i+1==lng&&temp.charAt(i)=='Y'){
                    str += 'Й';
                    continue;
                }else if(i+1==lng2&&temp.charAt(i)=='Y'){
                    str += 'Й';
                    continue;
                }else if(i+1<lng&&temp.charAt(i)=='Y'){
                    str += 'Ы';
                    continue;
                }else if(i+1<lng2&&temp.charAt(i)=='Y'){
                    str += 'Ы';
                    continue;
                }

                if (temp.charAt(i)=='C' && temp.charAt(i+1)=='H'){

                    str += 'Ч';
                    i+=1;
                    continue;

                }else if (temp.charAt(i)=='Y' && temp.charAt(i+1)=='O'){
                    str += 'Ё';
                    i+=1;
                    continue;

                }else if (temp.charAt(i)=='Z' && temp.charAt(i+1)=='H'){
                    str += 'Ж';
                    i+=1;
                    continue;
                }else if (temp.charAt(i)=='K' && temp.charAt(i+1)=='H'){
                    str += 'Х';
                    i+=1;
                    continue;
                }else if (temp.charAt(i)=='T' && temp.charAt(i+1)=='S'){
                    str += 'Ц';
                    i+=1;
                    continue;
                }else if (temp.charAt(i)=='Y' && temp.charAt(i+1)=='U'){
                    str += 'Ю';
                    i+=1;
                    continue;
                }else if (temp.charAt(i)=='Y' && temp.charAt(i+1)=='A'){
                    str += 'Я';
                    i+=1;
                    continue;
                }else if (temp.charAt(i)=='S' && temp.charAt(i+1)=='H'){
                    str += 'Ш';
                    i+=1;
                    continue;
                }
                else if (temp.charAt(i)=='S' && temp.charAt(i+1)=='C' && temp.charAt(i+2)=='H'){
                    str +='Щ';
                    i+=2;
                    continue;
                }
                else {
                    if(i<temp.length()&&temp.charAt(i)=='Y'){
                        str +=retran("Ы");
                    }else if(i<temp.length()&&temp.charAt(i)=='y'){
                        str +=retran("ы");
                    }else if(i==temp.length()&&temp.charAt(i)=='Y'){
                        str +=retran("Й");
                    }else if(i==temp.length()&&temp.charAt(i)=='y'){
                        str +=retran("й");
                    }else{
                        str +=retran(temp.charAt(i)+"");
                    }
                }
            }catch (Exception e){}

        }
        return str;
    }
    public static String retran(String str){
        if (str.trim().equals("")) return " ";
        else {
            String[] t = {"А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ч", "Ц", "Ш", "Щ", "Э", "Ю", "Я", "Ы", "Ъ", "Ь", "а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ч", "ц", "ш", "щ", "э", "ю", "я", "ы", "ъ", "ь"};
            String[] f = {"A", "B", "V", "G", "D", "E", "YO", "ZH", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "KH", "CH", "TS", "SH", "SCH", "E", "YU", "YA", "Y", "`", "'", "a", "b", "v", "g", "d", "e", "yo", "zh", "z", "i", "j", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ch", "c", "sh", "sch", "e", "yu", "ya", "y", "`", "'"};
            for (int i = 0; i < f.length; i++) {
                if (str.equals(f[i])) {
                    return t[i];
                }
            }
        }
        return "";
    }

}
