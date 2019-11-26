import java.io.*;
import java.util.*;

class DataItem{
    private String label;
    private String mnemonic;
    private String optional;
    private String operand;
    private String comment;
    private int format;
    private int address;
    private int opcode;

    public DataItem(String l, String m, int f, int a){
        label = l;
        mnemonic = m;
        format = f;
        address = a;
    }//constructor


    public String getLabel(){
        return label;
    }

    public String getKey(){
        return mnemonic;
    }//get key

    public int getFormat(){
        return format;
    }

    public void setAddress(int addr){
        address = addr;
    }

    public int getAddress(){
        return address;
    }

    public void setOpcode(int opc){
        opcode = opc;
    }

    public int getOpcode(){
        return opcode;
    }

    public void setOptional(String option){
        optional = option;
    }

    public String getOptional(){
        return optional;
    }

    public void setOperand(String oper){
        operand = oper;
    }

    public String getOperand(){
        return operand;
    }

    public void setComment(String cmt){
        comment = cmt;
    }

    public String getComment(){
        return comment;
    }

}//DataItem

//Hash table for instructions
class MnemHashTable {
    public DataItem[] hashArray;
    private int arraySize;
    private DataItem nonItem;

    public MnemHashTable(int size){
        arraySize = size;
        hashArray = new DataItem[arraySize];
        nonItem = new DataItem("-1", "-1", 0, 0);
    }//constructor

    public void insert(DataItem item){
        String key = item.getKey();
        int hashVal = hashFunc(key);

        //until empty cell or nonItem
        while(hashArray[hashVal] != null && !hashArray[hashVal].getKey().equals("-1")){
            ++hashVal;//go to next cell i.e linear probing
            hashVal %= arraySize;//wraparound if needed
        }//while
        hashArray[hashVal] = item;
    }//insert

    public void insertInstruct(String list[][]){
        DataItem item;
        for(int i = 0; i < list.length; i++){
            switch (list[i][0]){
                case "-1":
                    System.out.println("not a instruct");
                    list[i][2] = "0";
                    break;
                case "START":
                    item = new DataItem(" ", list[i][0], 0, 0);
                    insert(item);
                    break;
                case "RESW":
                    item = new DataItem(" ", list[i][0], 3, 0);
                    insert(item);
                    break;
                case "WORD":
                    item = new DataItem(" ", list[i][0], 3, 0);
                    insert(item);
                    break;
                case "RESB":
                    item = new DataItem(" ", list[i][0], 1, 0);
                    insert(item);
                    break;
                case "BYTE":
                    item = new DataItem(" ", list[i][0], 1, 0);
                    insert(item);
                    break;
                case "BASE":
                    item = new DataItem(" ", list[i][0], 0, 0);
                    insert(item);
                    break;
                case "END":
                    item = new DataItem(" ", list[i][0], 0, 0);
                    insert(item);
                    break;
                default:
                    item = new DataItem("-1", list[i][0], Integer.parseInt(list[i][2]), Integer.parseInt(list[i][1], 16));
                    insert(item);
                    break;
            }//switch
        }//for
    }//insertInstruct

    public int hashFunc(String key){
        int hashVal = 0;
        for(int j=0; j<key.length(); j++){
            int letter = key.charAt(j); //- 96; //get char code
            hashVal = (hashVal * 26 + letter) % arraySize; //mod
        }
        return hashVal;
    }//hashFunc


    public DataItem find(String key){
        int hashVal = hashFunc(key);

        while(hashArray[hashVal] != null){
            if(hashArray[hashVal].getKey().equals(key)) {
                return hashArray[hashVal];
            }

            ++hashVal; //linear probe
            hashVal %= arraySize; //wraparound

        }//while
        return nonItem; //couldn't find it
    }//find
}//MnemHashTable

class LabelHashTable {
    private DataItem[] hashArray;
    private int arraySize;
    private DataItem nonItem;

    public LabelHashTable(int size){
        arraySize = size;
        hashArray = new DataItem[arraySize];
        nonItem = new DataItem("-1", "-1", 0, 0);
    }//constructor

    public int[] makeAddresses(String[][] fullSet, MnemHashTable mnemonic){
        DataItem mtest;
        int[] addresses = new int[fullSet.length];
        int addr = 0;

        for(int j = 0; j < fullSet.length; j++){
            mtest = mnemonic.find(fullSet[j][1]); //label, mnemonic, optional, operand, comment
            switch (mtest.getKey()) {
                case "-1":
                    break;
                case "START":
                    addr += Integer.parseInt(fullSet[0][3], 16);
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    break;
                case "RESW":
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    addr += Integer.parseInt(fullSet[j][3].trim()) * 3;
                    break;
                case "WORD":
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    addr += 3;
                    break;
                case "RESB":
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    addr += Integer.parseInt(fullSet[j][3].trim());
                    break;
                case "BYTE":
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    addr += 1;
                    break;
                case "BASE":
                    addresses[j] = addr;
                    break;
                case "END":
                    addresses[j] = addr;
                    break;
                default:
                    addresses[j] = addr;
                    findLabel(fullSet[j][0]).setAddress(addr);
                    addr += mtest.getFormat();
                    break;
            }
        }
        return addresses;
    }//addressing

    public void displayTable(String[][] fullSet, MnemHashTable mnemonic){
        DataItem mtest;
        int[] addresses = makeAddresses(fullSet, mnemonic);

        //Initial display header
        System.out.println("*********************************************\n" +
                "University of North Florida: SIC/XE assembler\n" +
                "version date 12/26/2001\n" +
                "account: n00858026; Mon Nov  xxxxxxxxxxx\n" +
                "*********************************************\n" +
                "ASSEMBLER REPORT\n" +
                "----------------\n" +
                "     Loc   Object Code       Source Code\n" +
                "     ---   -----------       -----------");

        for(int j = 0; j < fullSet.length; j++){
            mtest = mnemonic.find(fullSet[j][1]); //label, mnemonic, optional, operand, comment
            switch (mtest.getKey()) {
                case "-1":
                    if (mtest.getKey().equals("-1"))
                        System.out.printf("%-4s %-5s                    %-10s%-9s%s%-6s%-8s\n", "---", "-----",
                                fullSet[j][0], //label
                                fullSet[j][1], //mnemoic
                                fullSet[j][2], //optional
                                fullSet[j][3], //operand
                                fullSet[j][4]);//comment
                    else
                        System.out.println(fullSet[j][4]);
                    break;
                case "START":
                    if (fullSet[j][0].equals("-1")) {
                        System.out.printf("%03x- %05x                    %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                        System.out.println("********** WARNING: Undefined Label At \"START\"");
                    } else
                        System.out.printf("%03x- %05x                    %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "RESW":
                    if (fullSet[j][0].equals("-1")) {
                        System.out.printf("%03x- %05x %-19s%-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                "FFFFFF",
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                        if (!isThere2(fullSet, j, fullSet[j][1])) {
                            System.out.println("********** WARNING Undefined label at " + addresses[j]);
                        }
                    } else
                        System.out.printf("%03x- %05x %-19s%-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                "FFFFFF",
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "WORD":
                    if (fullSet[j][0].equals("-1")){
                        System.out.printf("%03x- %05x %06x             %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                Integer.parseInt(fullSet[j][3]),
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                        if (!isThere2(fullSet, j, fullSet[j][1])) {
                            System.out.println("********** WARNING Undefined label at " + Integer.toHexString(addresses[j]).toUpperCase());
                        }
                    }
                    else
                        System.out.printf("%03x- %05x %06x             %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                Integer.parseInt(fullSet[j][3]),
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "RESB":
                    if(fullSet[j][0].equals("-1")) {
                        System.out.printf("%03x- %05x %-19s%-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                " ",
                                "FFFFFF",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                        if (!isThere2(fullSet, j, fullSet[j][1])) {
                            System.out.println("********** WARNING Undefined label at " + Integer.toHexString(addresses[j]).toUpperCase());
                        }
                    }
                    else
                        System.out.printf("%03x- %05x FFFFFF             %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "BYTE":
                    if(fullSet[j][0].equals("-1")) {
                        System.out.printf("%03x- %05x %02x                 %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                Integer.parseInt(fullSet[j][3]),
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                        if (!isThere2(fullSet, j, fullSet[j][1])) {
                            System.out.println("********** WARNING Undefined label at " + Integer.toHexString(addresses[j]).toUpperCase());
                        }
                    }
                    else
                        System.out.printf("%03x- %05x                   %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "BASE":
                    if(fullSet[j][0].equals("-1"))
                        System.out.printf("%03x- %05x                    %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    else
                        System.out.printf("%03x- %05x                    %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                case "END":
                    if(fullSet[j][0].equals("-1"))
                        System.out.printf("%03x- %05x                    %-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    else
                        System.out.printf("%-10s%-10s%-10s%s%-6s%-8s\n", Integer.toHexString(addresses[j]).toUpperCase(),
                                fullSet[j][0],
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    break;
                default:
                    DataItem searching;
                    searching = findLabel(fullSet[j][0]); //was looking for label in mnem table
                    //searching = objGen(searching, mnemonic);
                    int adrTemp;

                    findLabel(fullSet[j][0]).setAddress(addresses[j]);

                    if(!(searching == nonItem) && !(fullSet[j][0].equals("-1"))) {
                        adrTemp = searching.getAddress();
                        if (adrTemp == -1)
                            System.out.printf("%03x- %-4s                   %-10s%-9s%s%-6s%-8s\n", j,
                                    "----",
                                    fullSet[j][0],
                                    fullSet[j][1],
                                    fullSet[j][2],
                                    fullSet[j][3],
                                    fullSet[j][4]);
                        else
                            System.out.printf("%03x- %05x %-19s%-10s%-9s%s%-6s%-8s\n", j,  //line count
                                    addresses[j],       //address
                                    objGen(searching, mnemonic), //object code
                                    fullSet[j][0],      //label
                                    fullSet[j][1],      //mnemonic
                                    fullSet[j][2],      //optional
                                    fullSet[j][3],      //operand
                                    fullSet[j][4]);     //comment
                    }
                    else if(fullSet[j][0].equals("-1")) {
                        DataItem tempItem = new DataItem("-1", fullSet[j][1], mnemonic.find(fullSet[j][1]).getFormat(), addresses[j]);
                        tempItem.setOptional(fullSet[j][2]);
                        //System.out.println(fullSet[j][2]);
                        tempItem.setOperand(fullSet[j][3]);
                        tempItem.setOpcode(mnemonic.find(fullSet[j][1]).getAddress());

                        System.out.printf("%03x- %05x %-19s%-10s%-9s%s%-6s%-8s\n", j,
                                addresses[j],
                                objGen(tempItem, mnemonic),
                                " ",
                                fullSet[j][1],
                                fullSet[j][2],
                                fullSet[j][3],
                                fullSet[j][4]);
                    }
                    else
                        System.out.printf("%03x- %05x %-19x%-10s%-9s%s%-6s%-8s\n", j,  //line count
                                addresses[j],       //address
                                Integer.parseInt(objGen(searching, mnemonic)), //object code
                                fullSet[j][0],      //label
                                fullSet[j][1],      //mnemonic
                                fullSet[j][2],      //optional
                                fullSet[j][3],      //operand
                                fullSet[j][4]);     //comment
                    break;
            }
        }
    }//displayTable

    public void insert(DataItem item){
        String key = item.getLabel();
        int hashVal = hashFunc(key);

        //until empty cell or nonItem
        while(hashArray[hashVal] != null && hashArray[hashVal].getLabel() != "-1"){
            ++hashVal;//go to next cell i.e linear probing
            hashVal %= arraySize;//wraparound if needed
        }//while
        hashArray[hashVal] = item;
    }//insert

    public void insertLabel(String list[][], MnemHashTable mnemonic){
        DataItem temp;
        DataItem other;

        for(int i = 0; i < list.length; i++){

            //Checks if label was read in, assigns to object
            switch (list[i][0]){

                //no label was read in
                case "-1":
                    //System.out.println("broke");
                    break;

                //label exists, check for duplicates, bad mnem, or mnem without label and insert
                default:
                    DataItem item= new DataItem(list[i][0], list[i][1], 0, 0);
                    item.setOptional(list[i][2]);
                    item.setOperand(list[i][3]);
                    item.setOpcode(mnemonic.find(item.getKey()).getAddress());
                    other = findLabel(item.getLabel());
                    temp = mnemonic.find(list[i][1]);
                    if(temp.getKey().equals("-1")) {
                        System.out.println("ERROR: Invalid Mneumonic \"" + list[i][1] + "\" (ignoring line)");
                        break;
                    }
                    else if (other.getLabel().equals(item.getLabel())){
                        System.out.println("ERROR: Duplicate Label \"" + item.getLabel() + "\"");
                        insert(item);
                        break;
                    }
                    else if(list[i][0].equals("-1")) {
                        System.out.println("Error: Undefined Label At Address");
                        break;
                    }
                    else {
                        //System.out.println(list[i][1]);
                        item = new DataItem(list[i][0], list[i][1], temp.getFormat(), 0);
                        item.setOptional(list[i][2]);
                        item.setOperand(list[i][3]);
                        item.setComment(list[i][4]);
                        insert(item);
                    }
                    break;
            }//switch
        }//for
    }//insertInstruct

    public int hashFunc(String key){
        int hashVal = 0;
        for(int j=0; j<key.length(); j++){
            int letter = key.charAt(j); //- 96; //get char code
            hashVal = (hashVal * 26 + letter) % arraySize; //mod
        }
        return hashVal;
    }//hashFunc

    public DataItem findLabel(String label){
        int hashVal = hashFunc(label);

        while(hashArray[hashVal] != null){
            if(hashArray[hashVal].getLabel().equals(label)) {
                return hashArray[hashVal];
            }

            ++hashVal; //linear probe
            hashVal %= arraySize; //wraparound

        }//while
        return nonItem; //couldn't find it
    }//find

    public boolean isThere(String[][] fullset, int k, String test){
        for (int j = k-1; j>0;j--){
            if (fullset[j][0].equals(test)){
                return true;
            }
        }
        return false;
    }//isThere

    public boolean isThere2(String[][] fullset, int k, String test){
        int j = 0;
        for (j = k-1; j>0;j--){
            if (!fullset[j][1].equals(test)){
                break;
            }
        }
        if (!fullset[j+1][0].equals("-1")){
            return true;
        }
        return false;
    }//isThere

    public String objGen(DataItem element, MnemHashTable mnem){
        String[] myArr = new String[2];
        String operand;
        int length;
        int opcode;
        String objcode = "FFFFFF";
        int refAddr;
        int calcAddr;
        int flags = 0;

        if(element.getKey().equals("RSUB"))
            return "4F0000";

        length = mnem.find(element.getKey()).getFormat();

        //Assign opcode from mnemonic table
        opcode = mnem.find(element.getKey()).getAddress();
        //System.out.println(opcode);
        //System.out.println(length);
        //System.out.println(element.getLabel());

        operand = element.getOperand();


        switch(element.getFormat()) {
            case 2:
                break;

            case 3:
                //immediate addressing
                if (element.getOptional().equals("#")) {
                    opcode += 1;

                    //check for index in operand
                    if(operand.contains(",")) {
                        myArr = operand.split(",");
                        operand = myArr[0];
                        flags += 10;
                    }

                    if(operand.matches("[A-Za-z]*")) {
                        //pc or base relative addressing
                        refAddr = findLabel(operand).getAddress();
                        calcAddr = refAddr - element.getAddress();
                    }

                    else{
                        calcAddr = Integer.parseInt(operand, 16);

                        objcode = String.format("%2s", Integer.toHexString(opcode & 0xFF).toUpperCase()).replace(" ", "0")
                                + Integer.toHexString(flags & 0xF).toUpperCase()
                                + String.format("%3s", Integer.toHexString(calcAddr & 0xFFF).toUpperCase()).replace(" ", "0");
                        return objcode;
                    }

                    if (calcAddr > 2047)
                        flags += 0x4;
                    else
                        flags += 0x2;

                    objcode = String.format("%2s", Integer.toHexString(opcode & 0xFF).toUpperCase()).replace(" ", "0")
                            + Integer.toHexString(flags & 0xF).toUpperCase()
                            + String.format("%3s", Integer.toHexString(calcAddr & 0xFFF).toUpperCase()).replace(" ", "0");
                    return objcode;
                }

                //indirect addressing
                else if (element.getOptional().equals("@")) {

                }

                //no optional read in
                else if (element.getOptional().equals("-1") || element.getOptional().equals(" ")) {
                    opcode += 3; //N and I flags are on

                    //check for index in operand138
                    if(operand.contains(",")) {
                        myArr = operand.split(",");
                        operand = myArr[0];
                        flags += 10;
                    }

                    //pc or base relative addressing
                    refAddr = findLabel(operand).getAddress();
                    calcAddr = refAddr - element.getAddress();

                    if (calcAddr > 2047 || calcAddr < -2048)
                        flags += 0x4;
                    else
                        flags += 0x2;

                    objcode = String.format("%2s", Integer.toHexString(opcode)).replace(" ", "0")
                            + Integer.toHexString(flags)
                            + String.format("%3s", Integer.toHexString(calcAddr)).replace(" ", "0");
                    return objcode;
                } else {
                    System.out.println("somethings fucked with the optional argument");
                    return objcode;
                }
                break;

            case 4:
                if(element.getOptional().equals("#")) {
                    opcode += 1; //N and I flags are on

                    //check for index in operand138
                    if (operand.contains(",")) {
                        myArr = operand.split(",");
                        operand = myArr[0];
                        flags += 10;
                    }

                    //extended addressing is direct addressing
                    calcAddr = findLabel(operand).getAddress();

                    objcode = String.format("%2s", Integer.toHexString(opcode)).replace(" ", "0")
                            + Integer.toHexString(flags)
                            + String.format("%5s", Integer.toHexString(calcAddr)).replace(" ", "0");
                    return objcode;
                }

                else
                {
                    opcode += 3; //N and I flags are on

                    //check for index in operand138
                    if (operand.contains(",")) {
                        myArr = operand.split(",");
                        operand = myArr[0];
                        flags += 10;
                    }

                    //extended addressing is direct addressing
                    calcAddr = findLabel(operand).getAddress();

                    objcode = String.format("%2s", Integer.toHexString(opcode)).replace(" ", "0")
                            + Integer.toHexString(flags)
                            + String.format("%5s", Integer.toHexString(calcAddr)).replace(" ", "0");
                    return objcode;
                }
            default:
                System.out.println("Format not recognized");
                System.out.println(element.getFormat());
                break;
        }

        //element.setObject(opcode);

        return objcode;
    }

}//LabelHashTable

class PassOne{
    public static void main(String args[]) throws FileNotFoundException{

        String[][] sicNode = getInstruction(args[0]);//collect all the instructions of the file
        String[][] iptNode = getInput(args[1]);//collect all instructions in file

        int x = sicNode.length;
        int instructSize = getPrime(x*2); //calculate prime arraysize of 2n where n is the number of lines in the file

        //Create Hash Table for SICOPS file, used for referencing the input
        MnemHashTable Instructions = new MnemHashTable(instructSize);
        Instructions.insertInstruct(sicNode);

        //Read in a sic file, make its own hash table? compare to sicops table
        x = iptNode.length;

        System.out.println(" ");
        //System.out.println("---------------------------------------------------------");

        int inputSize = getPrime(x*2);

        LabelHashTable Inputs = new LabelHashTable(inputSize);
        Inputs.insertLabel(iptNode, Instructions);
        Inputs.displayTable(iptNode, Instructions);


    }//main

    public static int getPrime(int min){
        for(int j = min+1; true; j++){
            if( isPrime(j) )
                return j;
        }//for
    }//getPrime

    public static boolean isPrime(int n){
        for(int j = 2; (j*j <= n); j++){
            if( n % j == 0)
                return false;
        }//for
        return true;
    }

    public static String[][] getInstruction(String fileName) throws FileNotFoundException{
        File fileOne = new File(fileName);
        Scanner s1 = new Scanner(fileOne);
        String[][] instruct;
        String[] myArray;

        //count how many lines in file
        int lineCounter = 0;
        while(s1.hasNext()){
            String[] temp = s1.nextLine().split("\\s+");
            if(temp[0].equals("A"))
                break;
            lineCounter++;
        }//while

        instruct = new String[lineCounter][3];//initialize the string array

        //get the strings
        Scanner s2 = new Scanner(fileOne);
        int i = 0;
        while(s2.hasNext()){
            myArray = s2.nextLine().split("\\s+");
            if(myArray[0].equals("A"))
                return instruct;
            instruct[i][0] = myArray[0]; //mnemonic
            instruct[i][1] = myArray[1]; //opcode
            instruct[i][2] = myArray[2]; //format
            i++;
        }//while


        return instruct;
    }//getLines

    public static String[][] getInput(String myFile) throws FileNotFoundException{
        File fileTwo = new File(myFile);
        Scanner s1 = new Scanner(fileTwo);
        String[][] instruct;
        String[] myArray;
        String temp;
        char c;

        //line counter
        int lineCounter = 0;
        while(s1.hasNext()){
            temp = s1.nextLine();
            lineCounter++;
        }//while

        instruct = new String[lineCounter][5];

        Scanner s2 = new Scanner(fileTwo);
        for(int i = 0; i < lineCounter; i++){

            temp = s2.nextLine();
            temp += "                                                  ";
            int stringSize = temp.length();

            c = temp.charAt(0);
            switch (c){
                case '.':
                    instruct[i][0] = "-1";//label
                    instruct[i][1] = "-1";//mnemonic
                    instruct[i][2] = "-1";//operand
                    instruct[i][3] = "-1";//optional
                    instruct[i][4] = temp;//comment
                    break;
                case ' ':
                    //grab label
                    instruct[i][0] = "-1";
                    //grab mnemonic
                    instruct[i][1] = temp.substring(9, 16).trim();
                    //grab optional
                    instruct[i][2] = Character.toString(temp.charAt(18));
                    //grab operand
                    instruct[i][3] = temp.substring(19, 28).trim();
                    //grab comments
                    instruct[i][4] = temp.substring(31).trim();
                    break;
                default:
                    if(temp.substring(0, 7).trim().matches("[A-Za-z0-9]+")){
                        //grab label
                        instruct[i][0] = temp.substring(0, 8).trim();
                        //grab mnemonic
                        instruct[i][1] = temp.substring(9, 17).trim();
                        //grab optional
                        instruct[i][2] = Character.toString(temp.charAt(18));
                        //grab operand
                        instruct[i][3] = temp.substring(19, 28).trim();
                        //grab comments
                        if(stringSize >= 31) {
                            instruct[i][4] = temp.substring(31);
                        }
                    }

                    else{
                        System.out.println("ERROR BAD LABEL");
                        instruct[i][0] = "-2";
                    }

                    break;
            }
        }

        return instruct;
    }
}//n00858026