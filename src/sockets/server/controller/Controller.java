/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import sockets.server.model.Gameplay;
import sockets.server.model.Word;

/**
 *
 * @author Fredrik
 */
public class Controller {
    private final Word word = new Word();
    private final Gameplay gameplay = new Gameplay();
    private List<String> wordList;
    private String wordRand;
    private int wordTries;
    
    public List<String> createWords() throws IOException {
        return wordList = word.createWords();
    }
    
    public String getWord() {
        return wordRand = word.getWord(wordList);
    }
    
    public int getTries() throws IOException {
        return wordTries = word.getTries(wordRand);
    }
    
    public String guessHandler(String guess, String secretWord) {
        return gameplay.getGuessResult(guess, secretWord);
    }

    public String maskWord(Set<String> correctChars, String secretWord) {
        return gameplay.getWordMask(correctChars, secretWord);
    }
}
