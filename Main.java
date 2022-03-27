package mua;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Parser parser = new Parser();
        String s="";
        while(scanner.hasNext()){
            //s = s+ " "+ scanner.nextLine();
            s = scanner.nextLine();
            /*
            if (s.contains("read") && scanner.hasNext())
                s = s+ " "+ scanner.nextLine();
            else if (s.contains("[") && scanner.hasNext()){
                while(! isListComplete(s)){
                    s = s+ " "+ scanner.nextLine();
                }
            }
            if (!s.isEmpty()){
                parser.Set(s);
                parser.parse();
            }*/

            boolean hasError;
            int count = 0;
            do {
                hasError = false;

                try{
                    parser.Set(s);
                    parser.parse();
                }
                catch (Exception e){
                    hasError = true;
                    count+=1;
                    if(scanner.hasNext())
                        s = s+ " "+ scanner.nextLine();
                    if(count ==30){
                        System.out.println("dead");
                        break;
                    }
                }
            }while(hasError);
        }
    }
    public static boolean isListComplete(String s){
        boolean ret = false;
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '[') count++;
            else if (s.charAt(i) == ']') count--;
        }
        if(count == 0) ret = true;
        return ret;
    }
}