package mua;

import java.io.*;
import java.util.HashMap;

public class FileHandler {
    public void Saver(String filename, HashMap<String, String> map){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = new FileOutputStream(filename);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.flush();
            oos.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public HashMap<String, String> Loader(String filename){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        HashMap<String, String> map = new HashMap<String, String>();
        try{
            fis = new FileInputStream(filename);
            ois = new ObjectInputStream(fis);
            map = (HashMap<String, String>) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return map;
    }
    public String LoadAndExe(String filename){
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(filename));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr+" ");
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return sbf.toString();
    }
}
