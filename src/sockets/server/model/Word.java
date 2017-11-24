/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Fredrik
 */
public class Word {
    private final List<String> words = new ArrayList<>();
    private Random rand = new Random();
    
    /**
     *
     * @return returns a list of all words
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<String> createWords() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("rsrcs/words.txt"));        
        String line = reader.readLine();
        while(line != null) {
            String[] wordsLine = line.split(" ");
            for(String word : wordsLine) {
                words.add(word);
            }
            line = reader.readLine();
        }
        return words;
    }
    
    public String getWord(List<String> words) {
        String randomWord = words.get(rand.nextInt(words.size()));
        return randomWord;
    }
    
    public int getTries(String word) {
        int tries = word.length();
        return tries;
    }
}
