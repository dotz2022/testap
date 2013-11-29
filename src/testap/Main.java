import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Main{
        
        private static ArrayList<String> dir;
        private static ConcurrentHashMap<String,LinkedList<String>> theTable;
        private static ConcurrentLinkedQueue<String> workQ;
        
        /* reads in a filename and returns a string containing all the file's dependencies*/
        public static void process(String st) throws IOException{
        //inside a while work queue is not empty loop
                /*boolean found = false
                 * string filename = workq.poll
                 * string fullname = filename
                 * for everything in dir 
                 * full path = dir.get(i) + "/" + filename
                 * found = file(fullpath).isfile())
                 * if found: break :
                 * 
                 * handle exception - try catch block, print out file not found if exception thrown.
                 */
                System.out.println("processing "+st);
                boolean found = false;
                String path = st;
                String fullpath = st;
                for (int i = 0; i < dir.size(); i++){
                        fullpath = dir.get(i) + "/" + path;
                        System.out.println("fullpath "+fullpath);
                        found = new File(fullpath).isFile();
                        if (found){
                                System.out.println("file found");
                                System.out.println();
                                FileReader reader = new FileReader(fullpath);
                                Scanner in = new Scanner(reader);
                                String afterInclude = "";
                                String name = "";
                                LinkedList<String> newll;
                                while (in.hasNext()){
                                        if (in.next().equals("#include")){
                                                afterInclude = in.next();
                                                if (afterInclude.charAt(0) == '\"'){
                                                        name = afterInclude.substring(1,afterInclude.length()-1);
                                                        LinkedList<String> ll = new LinkedList<String>();
                                                        ll.add(name);
                                                        if (theTable.contains(name)){
                                                                continue;
                                                        }
                                                        newll = new LinkedList<String>();
                                                        theTable.put(name,newll);
                                                        workQ.add(name);
                                                }
                                        }
                                }
                                reader.close();
                                break;
                        }
                }
        }
        
        public static String getRoot(String file){
                return file.substring(0,file.length()-2);
        }
        
        public static String getExt(String file){
                return file.substring(file.length()-1,file.length());
        }
        
        public static String getObj(String file){
                String s = file.substring(0,file.length()-1) + "o";
                return s;
        }
        
        private static void printDependencies(LinkedList<String> toProcess, HashSet<String> printed) {
                while (!toProcess.isEmpty()){
                        String next = toProcess.poll();
                        LinkedList<String> deps = theTable.get(next);
                        while (!deps.isEmpty()){
                                String nextDep = deps.poll();
                                if (printed.contains(nextDep)){
                                        continue;
                                }
                                System.out.print(" "+nextDep);
                                printed.add(nextDep);
                                toProcess.add(nextDep);
                        }
                }
        }
        
        
        /* returns the correct usage of the input arguments*/
        public static void displayUsage(){
                System.err.println("usage: java -classpath . includeCrawler [-Idir] ... file.c|file.l|file.y");
        }
        
        public static void main(String[] args) throws IOException{
                
                //String cpath = System.getenv("CLASSPATH");
                String cpath = "/home/user/include:/usr/local/group/include";
                dir = new ArrayList<String>();
                String filename;
                
                /*
                 * determine the number of -Idir arguments
                 */
                int i;
                for (i = 0; i < args.length; i++){
                        if (!("-I".equals((args[i]).substring(0,2)))){ 
                                break;
                        }
                }
                
                int start = i;          //number of -Idir arguments
                
                
                /*
                 * for each -I tag, add it to the dir arraylist
                 */
                for (i = 0; i < start; i++){
                        dir.add(args[i].substring(2,args[i].length()));
                }
                
                /*
                 * determine the number of paths in environment variable, add each file into dir
                 */ 
                if (cpath != null){
                        Scanner pathScanner = new Scanner(cpath);
                        pathScanner.useDelimiter(":");
                        while (pathScanner.hasNext()){
                                dir.add(pathScanner.next());
                        }
                }
                
                /*
                 * adds current directory '.' into first part of arraylist
                 */
                dir.add(0, ".");
                
                theTable = new ConcurrentHashMap<String,LinkedList<String>>();
                workQ = new ConcurrentLinkedQueue<String>();
                
                /*
                 * for each file defined, map object file to source file in the hashtable
                 * 
                 */
                for (i = start; i < args.length; i++){
                        LinkedList<String> ll;
                        String ext =  getExt(args[i]);
                        if (ext.compareTo("c") != 0 && ext.compareTo("l") != 0 && ext.compareTo("y") != 0){
                                        displayUsage();                 // filename is invalid, so displays what should be the correct usage.
                        }
                        String obj = getObj(args[i]);
                        ll = new LinkedList<String>();
                        ll.add(args[i]);
                        theTable.put(obj,ll);
                        workQ.add(args[i]);
                        ll = new LinkedList<String>();
                        theTable.put(args[i],ll);
                }
                
                System.out.println("workQ: "+workQ);
                
                while (!workQ.isEmpty()){
                        filename = workQ.poll();
                        process(filename);
                }
                /*
                 * for each file specified, it's dependencies are output
                 */ 
                for (i = start; i < args.length; i++){
                        HashSet<String> printed = new HashSet<String>();
                        LinkedList<String> toProcess = new LinkedList<String>();
                        String obj;
                        obj = getObj(args[i]);
                        System.out.print(obj+":");
                        printed.add(obj);
                        toProcess.add(obj);
                        printDependencies(toProcess,printed);
                }
                
        }
}