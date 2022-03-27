package mua;

import java.util.HashMap;
import java.util.Stack;

public class Parser {
    HashMap<String, String> map = new HashMap<>();
    HashMap<String, String> globalMap = null;
    String s;
    String savedFile = "";
    boolean isReadList = false;

    public void Set(String string) {
        s = string;
    }
    public void setGlobalMap(HashMap<String ,String> gmap){
        globalMap = gmap;
    }
    public void setLocalMap(HashMap<String, String> lmap){
        map = lmap;
    }
    public String parse() {
        s = s.trim();
        //System.out.println("in parse");
        if(isReadList){
            s = s.replace("readlist", "readlist [");
            s = s.replace("[ ", "[");
            s = s + "]";
            isReadList = false;
        }
        String word = fetchWord();
        if (word.equals("make")) return make();
        else if (word.equals("print")) return print();
        else if (word.equals("thing")) return thing();
        else if (word.equals("read")) {          //read
            s = s.trim();
            word = fetchWord();
        }
        else if (word.equals("return")){
            s = s.trim();
            return parse();
        }
        else if (word.equals("export")){
            s = s.trim();
            String localKey = parse();
            String value = map.get(localKey);
            globalMap.put(localKey, value);
            return value;
        }
        else if (word.equals("erase")) return erase();
        else if (word.equals("run")) return run();
        else if (word.charAt(0) == ':') {          //:(thing)
            String key = word.substring(1);
            if(map.containsKey(key))
                return map.get(key);
            else return globalMap.get(key);
        } else if (word.charAt(0) == '\"') {         //word
            String realWord = word.substring(1);
            return realWord;
        } else if (word.charAt(0) == '(') return runExp(word);

            //check
        else if (word.equals("isname")) return isname();
        else if (word.equals("isbool")) return isbool();
        else if (word.equals("isnumber")) return isnumber();
        else if (word.equals("isword")) return isword();
        else if (word.equals("isempty")) return isEmpty();
        else if (word.equals("islist")) return isList();

            //compare
        else if (word.equals("eq")) return eq();
        else if (word.equals("gt")) return gt();
        else if (word.equals("lt")) return lt();

            //bool op
        else if (word.equals("and")) return and();
        else if (word.equals("or")) return or();
        else if (word.equals("not")) return not();
        else if (word.equals("if")) return myif();

            //operators
        else if (word.equals("mul")) return mul();
        else if (word.equals("mod")) return mod();
        else if (word.equals("add")) return add();
        else if (word.equals("sub")) return sub();
        else if (word.equals("div")) return div();

            // function
        else if (map.containsKey(word)) return runFunc(word);
        else if (globalMap!=null && globalMap.containsKey(word)) return runFunc(word);

            //operation on word & list (last part)
        else if (word.equals("readlist")){
            isReadList = true;
            s = s.trim();
            word = fetchWord();
            if(!word.isEmpty()) isReadList = false;
        }
        else if (word.equals("word")) return concat();
        else if (word.equals("sentence")) return sentence();
        else if (word.equals("list")) return list();
        else if (word.equals("join")) return join();
        else if (word.equals("first")) return first();
        else if (word.equals("last")) return last();
        else if (word.equals("butfirst")) return butFirst();
        else if (word.equals("butlast")) return butLast();

            //other operations
        else if (word.equals("erall")){
            map = new HashMap<>();
            return "true";
        }
        else if (word.equals("save")) return save();
        else if (word.equals("load")) return load();
        return word;
    }


    public String fetchWord() {
        String word = "";
        int i;
        if (s.charAt(0) == '[') {   //list
            int count = 0;
            for (i = 0; i < s.length(); i++) {
                word += s.charAt(i);
                if (s.charAt(i) == '[') count++;
                else if (s.charAt(i) == ']') count--;
                if (count == 0) break;
            }
            s = s.substring(i + 1);
            //System.out.println(word);
        } else if (s.charAt(0) == '(') {  //expression
            int count = 0;
            for (i = 0; i < s.length(); i++) {
                word += s.charAt(i);
                if (s.charAt(i) == '(') count++;
                else if (s.charAt(i) == ')') count--;
                if (count == 0) break;
            }
            s = s.substring(i + 1);
        } else {
            for (i = 0; i < s.length(); i++) {
                if (s.charAt(i) != ' ') {
                    word += s.charAt(i);
                } else break;
            }
            s = s.substring(i);
        }
        return word;
    }

    public String make() {
        String name = parse();
        String value = parse();
        map.put(name, value);
        return value;
    }

    public String erase() {
        String key = parse();
        String value = map.get(key);
        map.remove(key);
        return value;
    }

    public String run() {
        String list = parse();
        return runList(list);
    }

    public String runFunc(String funcName){
        //System.out.println("in: "+funcName);

        String funcList = map.containsKey(funcName)? map.get(funcName) : globalMap.get(funcName);
        HashMap<String, String> localMap = new HashMap<>();
        funcList = funcList.substring(1, funcList.length() - 1);
        funcList = funcList.trim();
        int splitPos = funcList.indexOf(']');
        String argList = funcList.substring(0,splitPos+1);
        String block = funcList.substring(splitPos+1);
        block = block.trim();

        //System.out.println(argList);
        //System.out.println(block);

        //count arg number
        argList = argList.substring(1, argList.length()-1);
        argList = argList.trim();
        if (!argList.isEmpty()){
            String [] args = argList.split("\\s+");
            for (String arg : args){
                String value = parse();
                //System.out.println(value);
                localMap.put(arg, value);
            }
        }


        boolean firstLevel = false;
        HashMap<String, String> tempMap = null;
        if(globalMap == null){
            firstLevel = true;
            setGlobalMap(map);
        }
        else{
            tempMap = map;
        }
        setLocalMap(localMap);
        String ret = runList(block);
        if(firstLevel){
            setLocalMap(globalMap);
            setGlobalMap(null);
        }
        else {
            setLocalMap(tempMap);
        }
        return ret;
    }
    public String runList(String list) {
        String ori = s;
        //System.out.println(list);

        Set(list.substring(1, list.length() - 1));
        String ret = "";
        while (s.length() > 0)
            ret = parse();
        Set(ori);
        return ret;
    }

    public String runExp(String exp) {
        String str = exp.substring(1, exp.length() - 1);
        str = str.replace("+", " + ");
        str = str.replace("-", " - ");
        str = str.replace("*", " * ");
        str = str.replace("/", " / ");
        str = str.replace("%", " % ");
        Stack<Double> numStack = new Stack<Double>();
        Stack<Character> opStack = new Stack<Character>();
        str = str.trim();

        while (str.length() > 0) {
            str = str.trim();
            char c = str.charAt(0);
            if (c == '+' || c == '-') {
                str = str.substring(1);
                if(opStack.size()==numStack.size() && c =='-'){
                    StringBuilder num = new StringBuilder();
                    num.append('-');
                    str = str.trim();
                    int i;
                    for (i = 0; i < str.length(); i++) {
                        if (str.charAt(i) >= '0' && str.charAt(i) <= '9') num.append(str.charAt(i));
                        else if (str.charAt(i) == '.') num.append(str.charAt(i));
                        else break;
                    }
                    str = str.substring(i);
                    //System.out.println(str);
                    numStack.push(Double.parseDouble(num.toString()));

                }
                else if (opStack.isEmpty()) opStack.push(c);
                else {
                    char topOp = opStack.pop();
                    double n2 = numStack.pop();
                    double n1 = numStack.pop();
                    double res = 0;
                    if (topOp == '+') res = n1 + n2;
                    else if (topOp == '-') res = n1 - n2;
                    else if (topOp == '*') res = n1 * n2;
                    else if (topOp == '/') res = n1 / n2;
                    else if (topOp == '%') res = n1 % n2;
                    opStack.push(c);
                    numStack.push(res);
                }
            } else if (c == '*' || c == '/' || c == '%') {
                str = str.substring(1);
                if (opStack.isEmpty()) opStack.push(c);
                else {
                    char topOp = opStack.pop();
                    if (topOp == '+' || topOp == '-') {
                        opStack.push(topOp);
                        opStack.push(c);
                    }
                    else {
                        double n2 = numStack.pop();
                        double n1 = numStack.pop();
                        double res=0;
                        if (topOp == '*') res = n1 * n2;
                        else if (topOp == '/') res = n1 / n2;
                        else if (topOp == '%') res = n1 % n2;
                        opStack.push(c);
                        numStack.push(res);
                    }
                }
            } else if (c >= '0' && c <= '9') {
                StringBuilder num = new StringBuilder();
                int i;
                for (i = 0; i < str.length(); i++) {
                    if (str.charAt(i) >= '0' && str.charAt(i) <= '9') num.append(str.charAt(i));
                    else if (str.charAt(i) == '.') num.append(str.charAt(i));
                    else break;
                }
                str = str.substring(i);
                numStack.push(Double.parseDouble(num.toString()));
            } else {
                String ori = s;
                Set(str);
                //System.out.println(str);
                String number = parse();
                //System.out.println(number);
                str = s;
                numStack.push(Double.parseDouble(number));
                Set(ori);
            }
        }
        while(!opStack.isEmpty()){
            char topOp = opStack.pop();
            double n2 = numStack.pop();
            double n1 = numStack.pop();
            double res = 0;
            if (topOp == '+') res = n1 + n2;
            else if (topOp == '-') res = n1 - n2;
            else if (topOp == '*') res = n1 * n2;
            else if (topOp == '/') res = n1 / n2;
            else if (topOp == '%') res = n1 % n2;
            numStack.push(res);
        }
        double res = numStack.pop();
        return res+"";
    }

    public String print() {
        String out = parse();
        if(out.charAt(0)=='[')
            out = out.substring(1,out.length()-1);
        System.out.println(out);
        return out;
    }

    public String thing() {
        String key = parse();
        if(map.containsKey(key))
            return map.get(key);
        else return globalMap.get(key);
    }

    //check type
    public String isname() {
        String word = parse();
        //boolean res = word.matches("[a-zA-Z][\\w]*");
        if(map.containsKey(word)) return "true";
        return "false";
    }

    public String isbool() {
        String value = parse();
        if (value.equals("true") || value.equals("false"))
            return "true";
        return "false";
    }

    public String isnumber() {
        String value = parse();
        boolean res = value.matches("-?[0-9]+.?[0-9]*");
        if (res) return "true";
        return "false";
    }

    public boolean isnumber(String value) {
        boolean res = value.matches("-?[0-9]+.?[0-9]*");
        if (res) return true;
        return false;
    }

    public String isword() {
        String value = parse();
        boolean res = value.matches("[\\S]*");
        if (res && (!isnumber(value))) return "true";
        return "false";
    }

    public String isEmpty(){
        String value = parse();
        if(value.isEmpty()) return "true";
        else{
            if(value.charAt(0)=='[' && value.charAt(1)==']'){
                return "true";
            }
        }
        return "false";
    }
    public String isList(){
        String value = parse();
        if(value.charAt(0)=='[') return "true";
        return "false";
    }

    //compare
    public String myif() {
        String cond = parse();
        String list1 = parse();
        String list2 = parse();
        if (cond.equals("true")) return runList(list1);
        return runList(list2);
    }

    public String eq() {
        String n1 = parse();
        String n2 = parse();
        if (isnumber(n1) && isnumber(n2)) {
            if (Double.parseDouble(n1) == Double.parseDouble(n2))
                return "true";
        } else {
            if (n1.equals(n2)) return "true";
        }
        return "false";
    }

    public String gt() {
        String n1 = parse();
        String n2 = parse();
        if (isnumber(n1) && isnumber(n2)) {
            if (Double.parseDouble(n1) > Double.parseDouble(n2))
                return "true";
        } else {
            if (n1.compareTo(n2) > 0) return "true";
        }
        return "false";
    }

    public String lt() {
        String n1 = parse();
        String n2 = parse();
        if (isnumber(n1) && isnumber(n2)) {
            if (Double.parseDouble(n1) < Double.parseDouble(n2))
                return "true";
        } else {
            if (n1.compareTo(n2) < 0) return "true";
        }
        return "false";
    }

    //bool operator
    public String and() {
        String o1 = parse();
        String o2 = parse();
        if (o1.equals("true") && o2.equals("true"))
            return "true";
        return "false";
    }

    public String or() {
        String o1 = parse();
        String o2 = parse();
        if (o1.equals("true") || o2.equals("true"))
            return "true";
        return "false";
    }

    public String not() {
        String o = parse();
        if (o.equals("false"))
            return "true";
        return "false";
    }

    //operator
    public String add() {
        String n1 = parse();
        String n2 = parse();
        double num1 = Double.parseDouble(n1);
        double num2 = Double.parseDouble(n2);
        return (num1 + num2) + "";
    }

    public String sub() {
        String n1 = parse();
        String n2 = parse();
        double num1 = Double.parseDouble(n1);
        double num2 = Double.parseDouble(n2);
        return (num1 - num2) + "";
    }

    public String mul() {
        String n1 = parse();
        String n2 = parse();
        double num1 = Double.parseDouble(n1);
        double num2 = Double.parseDouble(n2);
        return (num1 * num2) + "";
    }

    public String mod() {
        String n1 = parse();
        String n2 = parse();
        int num1 = Integer.parseInt(n1);
        int num2 = Integer.parseInt(n2);
        return (num1 % num2) + "";
    }

    public String div() {
        String n1 = parse();
        String n2 = parse();
        double num1 = Double.parseDouble(n1);
        double num2 = Double.parseDouble(n2);
        return (num1 / num2) + "";
    }

    public String concat(){
        String word1 = parse();
        String word2 = parse();
        return word1 + word2;
    }

    public String first(){
        String word = parse();
        String ret = "";
        if(word.charAt(0)=='['){//list
            String ori = s;
            Set(word.substring(1, word.length() - 1));
            ret = parse();
            Set(ori);
        }
        else
            ret = word.substring(0,1);
        return ret;
    }

    public String last(){
        String word = parse();
        String ret = "";
        if(word.charAt(0)=='['){//list
            ret = runList(word);
        }
        else
            ret = word.substring(word.length()-1);
        return ret;
    }

    public String join(){
        String list = parse();
        String value = parse();
        return list.substring(0,list.length()-1)+" "+value + "]";
    }

    public String butFirst(){
        String word = parse();
        String ret = "";
        if(word.charAt(0)=='['){//list
            String ori = s;
            Set(word.substring(1, word.length() - 1));
            parse();
            s = s.trim();
            ret = "[" + s + "]";
            Set(ori);
        }
        else
            ret = word.substring(1);
        return ret;
    }

    public String butLast(){
        String word = parse();
        String ret = "";
        String element = "";
        if(word.charAt(0)=='['){//list
            String ori = s;
            Set(word.substring(1, word.length() - 1));
            while (s.length() > 0){
                element = parse();
                if(s.length() >0)
                    ret = ret + element + " ";
            }
            Set(ori);
            ret = ret.trim();
            ret = "[" + ret + "]";
        }
        else
            ret = word.substring(0,word.length()-1);
        return ret;
    }
    public String sentence(){
        String value1 = parse();
        String value2 = parse();
        if(value1.charAt(0)=='[')
            value1 = value1.substring(1,value1.length()-1);
        if(value2.charAt(0)=='[')
            value2 = value2.substring(1,value2.length()-1);
        value1 = value1.trim();
        value2 = value2.trim();
        return "[" + value1 + " " + value2 + "]";
    }
    public String list(){
        String value1 = parse();
        String value2 = parse();
        return "[" + value1 + " " + value2 + "]";
    }
    public String save(){
        String filename = parse();
        savedFile = filename;
        FileHandler f = new FileHandler();
        f.Saver(filename, map);
        return filename;
    }

    public String load(){
        String filename = parse();
        FileHandler f = new FileHandler();
        if(filename.equals(savedFile)){
            HashMap<String, String> loaded = f.Loader(filename);
            map.putAll(loaded);
        }
        else{
            String code = f.LoadAndExe(filename);
            Set(code);
            //System.out.println(code);
            while(!s.isEmpty())
                parse();
        }
        return "true";
    }
}

