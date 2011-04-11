package com.Hunt800.Jackpot;

import java.io.*;
import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;

/**
 * Jackpot deck object
 * @author Hunt800
 */

public class Deck {
	private ArrayList<String> cardsLeft = new ArrayList<String>();
	private HashMap<Player, ArrayList<String>> playerCards = new HashMap<Player, ArrayList<String>>();
	private ArrayList<String> cardsInMiddle = new ArrayList<String>();
	
	public Deck(Player owner) {
		shuffleDeck();
	}
	public void shuffleDeck() {
		playerCards.clear();
		cardsInMiddle.clear();
		cardsLeft.clear();
		
		String cardVal = "";
		String cardSuit = "";
		for(Integer i = 0; i<13; i++){
			if(i < 9){
				Integer cV = i+2;
				cardVal = ChatColor.WHITE + cV.toString() + " of ";
			} else {
				if(i == 9){
					cardVal = ChatColor.WHITE + "Jack of ";
				}
				if(i == 10){
					cardVal = ChatColor.WHITE + "Queen of ";
				}
				if(i == 11){
					cardVal = ChatColor.WHITE + "King of ";
				}
				if(i == 12){
					cardVal = ChatColor.WHITE + "Ace of ";
				}
			}
			for(Integer j = 0; j<4; j++){
				if(j == 0){
					cardSuit = ChatColor.GRAY + "<Spades>";
				}
				if(j == 1){
					cardSuit = ChatColor.GRAY + "<Clubs>";
				}
				if(j == 2){
					cardSuit = ChatColor.RED + "<Diamonds>";
				}
				if(j == 3){
					cardSuit = ChatColor.RED + "<Hearts>";
				}
				
				cardsLeft.add(cardVal + cardSuit);
			}
		}
	}
	public String dealCard(Player forWho) {
		ArrayList<String> curCards = new ArrayList<String>();
		if(playerCards.containsKey(forWho)) {
			curCards = playerCards.get(forWho);
		}
		String cardDrawn = drawCard(false);
		curCards.add(cardDrawn);
		playerCards.put(forWho, curCards);
		return cardDrawn;
	}
	public String drawCard(Boolean toMiddle) {
		Integer index = randomInt(1,cardsLeft.size()) - 1;
		String cardToDraw = cardsLeft.get(index);
		if(cardToDraw.equals("null")) { cardToDraw = drawCard(toMiddle); }
		cardsLeft.set(index, "null");
		if(!toMiddle) {
			
		} else {
			cardsInMiddle.add(cardToDraw);
		}
		return cardToDraw;
	}
	public ArrayList<String> getCards(Player player){
		if(playerCards.containsKey(player)){
			return playerCards.get(player);
		} else {
			return new ArrayList<String>();
		}
	}
	public ArrayList<String> seeCards(){
		return cardsInMiddle;
	}
	public ArrayList<String> cardsLeft(){
		return cardsLeft;
	}
	public Integer randomInt(Integer min, Integer max){
	    	Random generator = new Random(); //Init rng
	    	Integer r = generator.nextInt(max) + min; //Generates a number between 1 and the sides of the dice
	    	return r;
	}
}
