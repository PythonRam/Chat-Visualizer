package users;

import Utils.Colors;
import Utils.enums.EnumMonths;
import algorithms.AlgorithmCMT;
import algorithms.matrioshka.Day;
import algorithms.matrioshka.Hour;
import algorithms.matrioshka.Month;
import algorithms.matrioshka.Year;
import interpreters.InterpreterWhatsapp;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import users.Person;

public class PersonManager{
    //Variable estatica con la ruta del chat a analizar
    private static String filePath = "..\\..\\Chats\\ChatBase(total).txt";
    InterpreterWhatsapp InterWhatsapp;
    List<Person> persons;
    
    public PersonManager(){
        //Crear más adelante una clases que gestione todo esto
        //Inicializamos la clase interprete que analizará y devolverá el chat de whatsapp
        InterWhatsapp = new InterpreterWhatsapp(filePath);
        //Inicializamos la lista de personas con todos sus mensajes así como la información pertinente del chat
        persons = InterWhatsapp.chatInterpret();
    }
    //Guardaremos el número total de personas acutales para reutilizarlo sin llamar al metodo
    private int totalPersons;
    //Creamos un array de cada "Persona" almacenando su extructura matrishka con todos los datos
    private LinkedHashMap<Integer, Year>[] personsMatrishka;
    //Totales globales
    private int messagesGlobal;
    private int wordGlobal;
    private int charsGlobal;
    private int daysGlobal;
    //Media de mensajes mensuales
    private double[][] grupalAverageMonths;
    //Arranque inicial del algoritmo
    public void startAlgorythm(){
        //Pruebas del algoritmo
        for (int i = 0, t = persons.size(); i < t; i++){
            //Inicia el algoritmo de conteo y medias (Cada clase Person, le enviará sus mensajes uno a uno para que los analice y guarde los datos deseados)
            persons.get(i).startAlgorythm();
            //Crea un log sencillo con todos los años, así como los meses, días y horas que tiene cada uno
            persons.get(i).createLogOfTheMatrioshkaStructure();
        }
        //Inizialización de variables auxiliares
        auxiliaryInitialization();
        
        //Calculos post almacenaje de mensajes en la matrioshka
        getTotalNumber();
        totalDaysChat();
        //Saca la media grupal de lo que se habla mensualmente en la conversación
        getGrupAverageMonths();
        //Exportar/crear los json necesarios para las gráficas una vez ya tenemos todos los números
        exportJSons();

    }
    //Inicialización de variables auxiliares que necesitamos para obtener más numero y crear los fichertos json
    public void auxiliaryInitialization(){
        totalPersons = persons.size();
        personsMatrishka = new LinkedHashMap[totalPersons];
        //Obtenermos la matrioshka de todas las personas
        for (int i = 0, t = totalPersons; i < t; i++)
            personsMatrishka[i] = persons.get(i).getMatrioshka();
        messagesGlobal = 0;
        wordGlobal = 0;
        charsGlobal = 0;
        daysGlobal = 0;
    }
     //Con este metodo obtenemos el numero total de mensajes,words y chars usando variables globales
    //El valor de cada variable global lo obtenemos en la clase "AlgorithmCMT.java"
    public void getTotalNumber(){
        //Suma de los tops globales de cada persona para agruparlo
        for(int i = 0; i < totalPersons; i++){
            messagesGlobal += persons.get(i).getMessagesGlobal();
            wordGlobal += persons.get(i).getWordsGlobal();
            charsGlobal += persons.get(i).getCharsGlobal();
        }
        System.out.println("Numero total de mensajes --> "+ Colors.ANSI_YELLOW + messagesGlobal + Colors.ANSI_RESET);
        System.out.println("Numero total de palabras --> "+ Colors.ANSI_YELLOW + wordGlobal + Colors.ANSI_RESET);
        System.out.println("Numero total de chars --> "+ Colors.ANSI_YELLOW + charsGlobal + Colors.ANSI_RESET);
    }
    //Calcula y anora la media mensual que se habla de manera grupal en una convresación
    public void getGrupAverageMonths(){
        //Guardo la cantidad de años que hay en este chat, dado que se usa mucho este valor
        int totalYears = persons.get(0).getAverageMonth().length;
        //Al igual que en su versión individual, se usa una variable local para almacenar el conteo de mensajes mensuales de cada persona
        double totalAverage = 0.0f;
        //Inicializamos el array 2D donde se almacenará la media grupal
        grupalAverageMonths = new double[totalYears][12];
        //Conjunto de bucles usados para extraer la media de cada media mensual individual, por cada año de las diferentes personas que han hablado en el chat.
        for (int year = 0, tY = persons.get(year).getAverageMonth().length; year < tY; year++){  
            for (int month = 0, tM = persons.get(year).getAverageMonth()[year].length; month < tM; month++){
                for (Person p : persons){
                    totalAverage += p.getOneAverageMonth(year, month);
                }
                //Se calcula la media de este mes y se almacena
                grupalAverageMonths[year][month] = totalAverage / totalPersons;
                totalAverage = 0;
            }
        }
        //Se muestra por consola la información de medias grupals. Cambiar más tarde por un fichero json
        System.out.println("\nMedia grupal por mes:");
        for (int year = 0, tY = totalYears; year < tY; year++){
            System.out.println("Añitos: " + year);
            for (int month = 0, tM = 12; month < tM; month++){
                System.out.println("Media global del mes " + month + ": " + grupalAverageMonths[year][month]);
            }
            System.out.println("\n");
        }
        
    }
    //Creación y expotación de ficheros JSon
    public void exportJSons(){
        //JSon de conteos básicos
        jSonCount();
        //Conteo palabras
        //Conteo letras
        //Más calculos y análisis para charts...
        
    }
    //Fichero JSon (chat): Conteo de mensajes 
    public void jSonCount(){
        //Dejo guardada la coletilla de cada indicador de fecha
        String beginingDate = "{\n\"date\": \"";
        StringBuilder jSonFileMessages = new StringBuilder();
        StringBuilder jSonFileWords = new StringBuilder();
        StringBuilder jSonFileChars = new StringBuilder();
        //Usamos un contador para los meses, ya que es necesaria su representación numérica en el json
        int counterMonths;
        //Abrimos brackets para empezar la extructura del json
        jSonFileMessages.append("[");
        jSonFileWords.append("[");
        jSonFileChars.append("[");
        //Estos fors son pareceidos a los de la matrioshka, con la diferentea de que estos se usan para recorrers todos los días de los años del calensario que han hablado estas personas en el chat.
        //Gracias a estos fors, podemos recorrer todas las fechas y anotarlas en el json
        for(HashMap.Entry<Integer, Year> y : personsMatrishka[0].entrySet()){
            counterMonths = 1;
            for(HashMap.Entry<String, Month> m : personsMatrishka[0].get(y.getKey()).getAllMonths().entrySet()){
                
                for(Day d : personsMatrishka[0].get(y.getKey()).getOneMonth(m.getKey()).getDays()){
                    //Escribimos la fecha en el json
                    jSonFileMessages.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",\n");
                    jSonFileWords.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",\n");
                    jSonFileChars.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",\n");
                    //Limite de personas preparado de antemano
                    //Dentro de esta fecha, anotamos las personas correspondientes, y el número de mensajes que tiene (preparado para recibir un número indefinído de personas)
                    for (int i = 0; i < totalPersons; i++){
                        if(i == totalPersons-1){
                            jSonFileMessages.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount() + "\n");
                            jSonFileWords.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getWordCount() + "\n");
                            jSonFileChars.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getCharCount() + "\n");
                        }else{
                            jSonFileMessages.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount() + ",\n");
                            jSonFileWords.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getWordCount() + ",\n");
                            jSonFileChars.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getCharCount() + ",\n");
                        }    
                    }
                    //Se cierra esta fecha
                    jSonFileMessages.append("}, ");
                    jSonFileWords.append("}, ");
                    jSonFileChars.append("}, ");
                    /*for (Hour h : d.getHours()){
                        jSonFile.append(y.getKey() + "/" + m.getKey() + "/" + d.getName() + " - " + h.getName() + ":00\n");
                        //System.out.println(y.getKey() + "/" + m.getKey() + "/" + d.getName() + "\n");
                        for (int i = 0; i < totalPersons; i++){
                            jSonFile.append(persons.get(i).getName() + ": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getOneHour(h.getName()).getMessageCount() + "\n");
                            //System.out.println(persons.get(i).getName() + ": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getOneHour(h.getName()).getMessageCount() + "\n");
                        }
                    }*/
                }
                //Tenemos en cuenta que el counter de meses no se pase de 12
                counterMonths++;
                if(m.getKey().equals(EnumMonths.DECEMBER))
                    counterMonths = 1;
            }
        }
        //Elimina la última coma innecesaria
        jSonFileMessages.setLength(jSonFileMessages.length() - 2);
        jSonFileWords.setLength(jSonFileWords.length() - 2);
        jSonFileChars.setLength(jSonFileChars.length() - 2);
        //Cerramos la extructura del json
        jSonFileMessages.append("]");
        jSonFileWords.append("]");
        jSonFileChars.append("]");
        //Por último, creamos el fichero json
        try(FileOutputStream oFileMessages = new FileOutputStream("MessageCount.json", false)){
            oFileMessages.write(jSonFileMessages.toString().getBytes());
        } 
        catch (Exception e){
            System.out.println("Error: " + e);
        }
        
        try(FileOutputStream oFileWords = new FileOutputStream("WordsCount.json", false)){
            oFileWords.write(jSonFileWords.toString().getBytes());
        } 
        catch (Exception e){
            System.out.println("Error: " + e);
        }
        
        try(FileOutputStream oFileChars = new FileOutputStream("CharsCount.json", false)){
            oFileChars.write(jSonFileChars.toString().getBytes());
        } 
        catch (Exception e){
            System.out.println("Error: " + e);
        }
    }
    //Configuraciones de gráficos (no sé si se usará). Van al final de los fichros json
    private static final String conf = "\nhelloConf;";
    
    //Metodo que saca el numero total de dias de una conversación
    public void totalDaysChat(){
        for(HashMap.Entry<Integer, Year> y : personsMatrishka[0].entrySet()){
            for(HashMap.Entry<String, Month> m : personsMatrishka[0].get(y.getKey()).getAllMonths().entrySet()){
                for(Day d : personsMatrishka[0].get(y.getKey()).getOneMonth(m.getKey()).getDays()){
                    boolean daysTalked = false;
                    for (int i = 0; i < totalPersons && !daysTalked; i++){
                        if(personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount()>0 ){
                            daysGlobal++;
                            daysTalked = true;
                        }
                    }
                   
                }
            }
        }
        System.out.println("Dias totales hablados --> "+ Colors.ANSI_YELLOW +daysGlobal+ Colors.ANSI_RESET); 
    }

}