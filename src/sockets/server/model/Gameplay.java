/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.model;

import java.util.Set;

/**
 *
 * @author Fredrik
 */
public class Gameplay {
    private String result;
    private String replacePattern;
    private String maskedWord;
    
    /**
     *
     * @param guess is tested through some logical tests
     * @param secretWord is the sought after word, compared with guess
     * @return returns a string identifier
     */
    public String getGuessResult(String guess, String secretWord) {
        if(guess.length() == 1) {
            if(secretWord.indexOf(guess)!=-1) {
                result = "t";
                return result;
            } else {
                result = "f";
                return result;
            }
        } else {
            if(secretWord.equals(guess)) {
                result = "true";
                return result;
            } else {
                result = "false";
                return result;
            }
        }
    }

    /**
     *
     * @param correctChars contains the correct guesses
     * @param secretWord is the sought after word
     * @return returns correct mask of word
     */
    public String getWordMask(Set<String> correctChars, String secretWord) {
        replacePattern = "(?i)[^";
        for (String correctChar : correctChars) {
            replacePattern += correctChar;
        }
        replacePattern += "\\-]";

        maskedWord = secretWord.replaceAll(replacePattern, "_ ");
        
        return maskedWord;
    }
}