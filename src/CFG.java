import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CFG {


    HashMap<Character, Set<String>> cfg;
    ArrayList<Character> keyList = new ArrayList<>();
    Set<Character> setV = new HashSet<>();
    ArrayList<String> powerSet = new ArrayList<>();

    CFG() {
        this.cfg = new HashMap<>();
    }


    public void readFromFile() throws FileNotFoundException {
        char key;
        Set<String> variables = new HashSet<>();
        Scanner keyboard = new Scanner(System.in);
        System.out.printf("%s", "Enter file path: ");

        String filePath = keyboard.nextLine();
        File inputFile = new File(filePath);
        Scanner fileInput = new Scanner(inputFile);
        try {
            StringBuilder token = new StringBuilder();


            while (fileInput.hasNext()) {
                token.append(fileInput.nextLine());
                while (token.length() > 0) {
                    key = token.charAt(0);
                    keyList.add(key);
                    token.deleteCharAt(0);
                    if (token.charAt(0) == '-') {
                        token.deleteCharAt(0);
                        variables.addAll(Arrays.asList(token.toString().split("[|]")));
                        token.setLength(0);
                    }
                    cfg.put(key, new HashSet<>(variables));
                    variables.clear();
                }
            }
        } catch (InputMismatchException e) {
            System.out.printf("%s", "Check file for bad data.");
        }
    }
    public void removeERules(){
        substituteEmptyRule();
        addToSetV();
        convertSetArray();
        replaceNullableSymbols();
    }

    private void substituteEmptyRule() {
        ArrayList<Character> deletedKeys = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            Set<String> temp = cfg.get(keyList.get(i));
            if (temp.contains("0") && temp.size() == 1) {
                deletedKeys.add(keyList.get(i));
                cfg.remove(keyList.get(i));
                keyList.remove(i);
            }
        }
        replaceDeletedKey(deletedKeys);
    }

    private void replaceDeletedKey(ArrayList<Character> deletedKeys) {
        ArrayList<String> removeRule = new ArrayList<>();
        ArrayList<String> addRule = new ArrayList<>();

        for (Character delKey : deletedKeys) {
            for (Character key : keyList) {
                if (cfg.get(key).contains(delKey.toString())) {
                    cfg.get(key).remove(delKey.toString());
                    cfg.get(key).add("0");
                }
                for(String rule: cfg.get(key)){
                    if(rule.contains(delKey.toString())){
                        for(int i=0; i < rule.length(); i++){
                            if(rule.charAt(i) == delKey){
                                removeRule.add(rule);
                                addRule.add(rule.replace(Character.toString(delKey),""));
                            }
                        }
                    }
                }
                cfg.get(key).removeAll(removeRule);
                removeRule.clear();
                cfg.get(key).addAll(addRule);
                addRule.clear();
            }
        }
    }
    private void addToSetV() {

        for (Character key : keyList) {
            if (cfg.get(key).contains("0")) {
                setV.add(key);
                cfg.get(key).remove("0");
            }
        }
        boolean added = true;
        boolean include = false;
        while (added) {
            for (Character key : keyList) {
                Set<String> temp = cfg.get(key);
                for (String variables : temp) {
                    if (setV.contains(variables.charAt(0))) {
                        include = true;
                        break;
                    }
                }
                if (include && !setV.contains(key)) {
                    setV.add(key);
                    added = true;
                } else
                    added = false;
                include = false;
            }
        }
    }
    private void convertSetArray(){
        char[] temp = new char[setV.size()];

        int i=0;
        for(Character c: setV){
            temp[i] = c;
            i++;
        }
        getPowerSet(temp, temp.length);
    }
    private void getPowerSet(char []set, int set_size)
    {   // found power set at:  https://www.geeksforgeeks.org/power-set/

        StringBuilder buffer = new StringBuilder();
        long pow_set_size = (long)Math.pow(2, set_size);
        int counter, j;


        for(counter = 0; counter < pow_set_size; counter++)
        {
            for(j = 0; j < set_size; j++)
            {
                if((counter & (1 << j)) > 0)
                    buffer.append(set[j]);
            }
            powerSet.add(buffer.toString());
            buffer.setLength(0);
        }
    }
    private void replaceNullableSymbols(){
        Set<String> temp = new HashSet<>();

        for(Character key: keyList){
            for (String rule : cfg.get(key)) {
                String originalRule = rule;
                for (String nullable : powerSet) {
                    for (int i = 0; i < nullable.length(); i++) {
                        int wordLength = rule.length();
                        rule = rule.replace(Character.toString(nullable.charAt(i)), "");
                        if(wordLength - rule.length() > 1)
                            moreThanOneOccurrence(originalRule,nullable.charAt(i),temp);
                    }
                    if (!rule.equals("") && !rule.equals(key.toString()))
                        temp.add(rule);
                    rule = originalRule;
                }
            }
            //add new rules to cfg
            cfg.get(key).addAll(temp);
            temp.clear();
        }
    }
    private void moreThanOneOccurrence(String rule, Character c, Set<String> temp){
        //Rules containing repeated variables: S-> ASA
        int[] position = new int[rule.length()];


        for(int i=0; i<rule.length(); i++){
            if(rule.charAt(i) == c)
                position[i] = 1;
        }
        StringBuilder modifyRule = new StringBuilder(rule);

        for(int i=0; i<rule.length(); i++){
            if(position[i] == 1) {
                modifyRule.deleteCharAt(i);
                temp.add(modifyRule.toString());
            }

            modifyRule.setLength(0);
            modifyRule.append(rule);
        }
    }
    public void removeUnitRule(){
        ArrayList<String> remove = new ArrayList<>();
        boolean change = true;
        while(change) {
            change = false;

            for (Character key : keyList) {
                for (String rule : cfg.get(key)) {
                    if (rule.length() == 1 && keyList.contains(rule.charAt(0))) {
                        remove.add(rule);
                    }
                }
                for (String rule : remove) {
                    if (rule.charAt(0) != key) {
                        cfg.get(key).remove(rule);
                        cfg.get(key).addAll(cfg.get(rule.charAt(0)));
                        if(cfg.get(key).contains(key.toString())) {
                            cfg.get(key).remove(key.toString());
                            change = true;
                        }
                    }
                }
                remove.clear();
            }
        }
    }

    public void removeUselessRule(){
        addTerminalsToSetV();
        deleteUselessRule();
       deleteUnreachableRule();
    }
    public void addTerminalsToSetV(){
        setV.clear();

        //Continue if new variables were added to set V
        boolean added = true;
        boolean include = false;
        while(added){
            added = false;
            for(Character key: keyList){
                for(String rule: cfg.get(key)){
                    for(int i=0; i<rule.length(); i++){
                        if(!keyList.contains(rule.charAt(i)) || setV.contains(rule.charAt(i))){
                            include = true;
                        }
                        else{
                            include = false;
                            break;
                        }
                    }
                    if(include && !setV.contains(key)){
                        setV.add(key);
                        added = true;
                        break;
                    }
                }
            }
        }
    }
    public void deleteUselessRule(){

        ArrayList<Character> deletedKeys = new ArrayList<>();
        for (Character key : keyList){
            if(!setV.contains(key)){
                cfg.remove(key);
                deletedKeys.add(key);
            }
        }
        for(Character delKey: deletedKeys){
            keyList.remove(delKey);
        }
        for(Character delKey: deletedKeys){
            for(Character key: keyList){
                cfg.get(key).removeIf(rule -> rule.contains(delKey.toString()));
            }
        }
    }
    public void deleteUnreachableRule(){
        setV.clear();  //Collect rules reachable from the start
        Character key;
        Queue<Character> ruleReached = new ArrayDeque<>();

        //Add start to rules that are reachable
        ruleReached.offer(keyList.get(0));
        setV.add(keyList.get(0));

        while(!ruleReached.isEmpty()){
            key = ruleReached.poll();
            for (String rule : cfg.get(key)) {
                for (int i = 0; i < rule.length(); i++){
                    if(keyList.contains(rule.charAt(i)) && !setV.contains(rule.charAt(i))) {
                        ruleReached.offer(rule.charAt(i));
                        setV.add(rule.charAt(i));
                    }
                }
            }
        }
        deleteUselessRule();
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();

        for (Character key : keyList) {
            temp.append(key + "-> ");
            Iterator<String> strings = cfg.get(key).iterator();
            while (strings.hasNext()) {
                temp.append(strings.next());
                if (strings.hasNext())
                    temp.append(" | ");
            }
            temp.append("\n");
        }
        temp.append("\n");
        return temp.toString();
    }
}
