/**
 * www.TheAIGames.com 
 * Heads Up Omaha pokerbot
 *
 * Last update: May 07, 2014
 *
 * @author Jim van Eeden, Starapple
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */


package bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import poker.Card;
import poker.HandOmaha;
import poker.PokerMove;

import com.stevebrecher.HandEval;

/**
 * This class is the brains of your bot. Make your calculations here and return the best move with GetMove
 */
public class BotStarter implements Bot {
	
	final int NUM_SIMS = 20000;
	

	/**
	 * Implement this method to return the best move you can. Currently it will return a raise the average ordinal value 
	 * of your hand is higher than 9, a call when the average ordinal value is at least 6 and a check otherwise.
	 * As you can see, it will only consider it's current hand, not what's on the table.
	 * @param state : The current state of your bot, with all the (parsed) information given by the engine
	 * @param timeOut : The time you have to return a move
	 * @return PokerMove : The move you will be doing
	 */
	@Override
	public PokerMove getMove(BotState state, Long timeOut) {
		final long startTime = System.currentTimeMillis();
		HandOmaha hand = state.getHand();
		HandEval.HandCategory handCategory = getHandCategory(hand, state.getTable());
		String handCategoryString = handCategory.toString();
		System.err.printf("my hand is %s, opponent action is %s, pot: %d\n", handCategoryString, state.getOpponentAction(), state.getPot());
		String result;
		int betAmount;
		
		if (state.getTable().length == 0) {
			String preFlopMove = startingHandEval(hand);
			
			if (preFlopMove.equals("raise")) {
				betAmount = 2*state.getBigBlind();
			} else if (checkStackProtection(state)) {
				if (preFlopMove.equals("call")) {
					betAmount = state.getAmountToCall();
				} else {
					betAmount = 0;
				}
			} else {
				preFlopMove = "check";
				betAmount = 0;
			}
			state.setOpponentMove(null);
			state.setAmountToCall(0);
			System.err.println("Time to move: " + state.getTimePerMove());
			final long endTime = System.currentTimeMillis();
			System.err.println("Total execution time: " + (endTime - startTime) );
			return new PokerMove(state.getMyName(), preFlopMove, betAmount);
		} else {
			ArrayList<Card> currentDeck = createDeck(hand, state.getTable());
			result = monteSim(currentDeck, state);
			if (result.equals("call")) {
				betAmount = state.getAmountToCall();
			} else if (result.equals("raise")) {
				betAmount = state.getPot()/2;
			} else {
				betAmount = 0;
			}
			state.setOpponentMove(null);
			state.setAmountToCall(0);
			System.err.println("Time to move: " + state.getTimePerMove());
			final long endTime = System.currentTimeMillis();
			System.err.println("Total execution time: " + (endTime - startTime) );
			return new PokerMove(state.getMyName(), result, betAmount); 
		}

	}
	
	public String startingHandEval(HandOmaha hand) {
		int[] startingHandHeights = {
			hand.getCard(0).getHeight().ordinal(),
			hand.getCard(1).getHeight().ordinal(),
			hand.getCard(2).getHeight().ordinal(),
			hand.getCard(3).getHeight().ordinal()
		};
		
		int[] startingHandSuits = {
			hand.getCard(0).getSuit().ordinal(),
			hand.getCard(1).getSuit().ordinal(),
			hand.getCard(2).getSuit().ordinal(),
			hand.getCard(3).getSuit().ordinal()		
		};
		
		Map<Integer, Integer> heightMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> suitMap = new HashMap<Integer, Integer>();
		
		//Check for hands that are three of a kind or four of a kind and hands
		//that have four cards of the same suit.
		for (int k = 0; k < 4; k++) {
			Integer val = new Integer(startingHandHeights[k]);
			Integer count = heightMap.get(val);
			if (count == null) {
				heightMap.put(val, 1);
			} else {
				heightMap.put(val, count + 1);
			}
			
			Integer suit = new Integer(startingHandSuits[k]);
			Integer suitCount = suitMap.get(suit);
			if (suitCount == null) {
				suitMap.put(suit, 1);
			} else {
				suitMap.put(suit, suitCount + 1);
			}
		}
		
		if (heightMap.containsValue(3) || heightMap.containsValue(4) || suitMap.size() == 1) {
			return "check";
		}
			
		//Raise if we have top 30 hand, otherwise call.
		for (int i = 0; i < 4; i++) {
			if (heightMap.get(12) != null && heightMap.get(12) == 2) {
				return "raise";
			} else if (heightMap.get(11) != null && heightMap.get(11) == 2) {
				if (heightMap.size() == 2) {
					if (heightMap.containsKey(10) || heightMap.containsKey(9) || heightMap.containsKey(8)) {
						return "raise";
					} else {
						return "call";
					}
				} else {
					for (int j = 0; j < 8; j++) {
						if (heightMap.containsKey(j)) {
							return "call";
						}
					}
				}
			} else if (heightMap.get(10) != null && heightMap.get(10) == 2) {
				if (heightMap.size() == 2) {
					if (heightMap.containsKey(9) || heightMap.containsKey(8)) {
						return "raise";
					}
				} else {
					for (int j = 0; j < 8; j++) {
						if (heightMap.containsKey(j)) {
							return "call";
						}
					}
				}
			} else if (heightMap.get(9) != null && heightMap.get(9) == 2) {
				if (heightMap.size() == 2) {
					if (heightMap.containsKey(8)) {
						return "raise";
					} else {
						return "call";
					}
				} else {
					if (heightMap.containsKey(8) && heightMap.containsKey(7)) {
						return "raise";
					} else {
						return "call";
					}
				}
			}
		}	
		return "call";
	}
	
	public boolean checkStackProtection(BotState state) {
		int myStack = state.getmyStack();
		int betAmt = state.getAmountToCall();
		int bigBlind = state.getBigBlind();
		if ((myStack - betAmt) < (bigBlind * 4)) {
			return false;
		} else {
			return true;
		}
	}
	
	public String monteSim(ArrayList<Card> tempDeck, BotState state) {
		int checkCounter = 0;
		int callCounter = 0;
		int raiseCounter = 0;
		
		if (state.getAmountToCall() == 0) {
			for (int i = 0; i < NUM_SIMS; i++) {
				ArrayList<Card> realDeck = new ArrayList<Card>();
				realDeck.addAll(tempDeck);
				MonteCarlo simCheck = new MonteCarlo(realDeck, state, "check");
				simCheck.runSim();
				checkCounter += simCheck.getResult();
			}
			
			for (int i = 0; i < NUM_SIMS; i++) {
				ArrayList<Card> realDeck = new ArrayList<Card>();
				realDeck.addAll(tempDeck);
				MonteCarlo simCheck = new MonteCarlo(realDeck, state, "raise");
				simCheck.runSim();
				raiseCounter += simCheck.getResult();
			}
			
			System.err.println("checkCounter: " + checkCounter + ". raiseCounter: " + raiseCounter);
			if (checkCounter > raiseCounter) {
				return "check";
			} else {
				return "raise";
			}
		} else {
			checkCounter = (-1 * NUM_SIMS * state.getPot());
			
			for (int i = 0; i < NUM_SIMS; i++) {
				ArrayList<Card> realDeck = new ArrayList<Card>();
				realDeck.addAll(tempDeck);
				MonteCarlo simCheck = new MonteCarlo(realDeck, state, "call");
				simCheck.runSim();
				callCounter += simCheck.getResult();
			}
			
			for (int i = 0; i < NUM_SIMS; i++) {
				ArrayList<Card> realDeck = new ArrayList<Card>();
				realDeck.addAll(tempDeck);
				MonteCarlo simCheck = new MonteCarlo(realDeck, state, "raise");
				simCheck.runSim();
				raiseCounter += simCheck.getResult();
			}
			
			System.err.println("checkCounter: " + checkCounter + ". callCounter: " + callCounter + ". raiseCounter: " + raiseCounter);
			if (checkCounter > callCounter && checkCounter > raiseCounter) {
				return "check";
			} else if (callCounter > checkCounter && callCounter > raiseCounter) {
				return "call";
			} else {
				return "raise";
			}
		}
	}
	
	public ArrayList<Card> createDeck (HandOmaha hand, Card[] table) {
	        ArrayList<Card> deck = new ArrayList<Card>();
	        ArrayList<String> usedCards = new ArrayList<String>();
	        
	        for (int i = 0; i < hand.getNumberOfCards(); i++) {
	            usedCards.add(hand.getCard(i).toString());
	        }
	        
	        for (int j = 0; j < table.length; j++) {
	        	usedCards.add(table[j].toString());
	        }
	       
	        for (int i = 0; i < 52; i++) {
	            Card tempCard = new Card(i);
	            if (usedCards.contains(tempCard.toString())) {
	                continue;
	            }
	            deck.add(tempCard);
	        }
	        return deck;
	}
	
	/**
	 * Quite a tedious method to check what we have in our hand. With 5 cards on the table we do 60(!) checks: all possible
	 * combinations of 2 out of 4 cards (our hand) times all possible combinations of 3 out of 5 cards (the table).
	 * For less cards on the table we need less calculation. This uses the com.stevebrecher package to get hand strength.
	 * @param hand : cards in hand
	 * @param table : cards on table
	 * @return HandCategory with what the bot has got, given the table and hand
	 */
	public HandEval.HandCategory getHandCategory(HandOmaha hand, Card[] table) {
		int strength = 0;
		
		// Try all possible combinations of 2 out of 4 cards for what we have in our hand (6 possibilities)
		for(int i=0; i<hand.getNumberOfCards()-1; i++) {
			for(int j=i+1; j<hand.getNumberOfCards(); j++) {
				
				if( table == null || table.length == 0 ) { // The table is empty, so we just check what we have in our hand and a pair is the best we can do
					if( hand.getCard(i).getHeight() == hand.getCard(j).getHeight() ) { // If two cards have the same height:
						return HandEval.HandCategory.PAIR; // We found a pair; return that we have a pair
					}
					else if ( i == hand.getNumberOfCards() - 2 && j == hand.getNumberOfCards() - 1 ) { // Last pair of cards
						return HandEval.HandCategory.NO_PAIR; // If we reach this we didn't find a pair, so return NO_PAIR
					}
					
				} else { // There are cards on the table
					long handCode = hand.getCard(i).getNumber() + hand.getCard(j).getNumber();
					
					if ( table.length == 3 ) { // Easy, because we must use all 3 cards on the table for evaluation
						for(int c=0; c<table.length; c++) {
							handCode += table[c].getNumber(); 
						}
						strength = Math.max(strength, HandEval.hand5Eval(handCode));
					}
					else if ( table.length == 4 ) { // We need to evaluate all combinations of 3 out of 4 cards (4 possibilities)
						for(int k=0; k<table.length; k++) {
							handCode = hand.getCard(i).getNumber() + hand.getCard(j).getNumber();
							for(int c=0; c<table.length; c++) 
								if(c != k)
									handCode += table[c].getNumber();
							strength = Math.max(strength, HandEval.hand5Eval(handCode));
						}
					}
					else if ( table.length == 5 ) { // We need to evaluate all combinations of 3 out of 5 cards (10 possibilities)
						for(int k=0; k<table.length-2; k++)
							for(int l=k+1; l<table.length-1; l++)
								for(int m=l+1; m<table.length; m++)
								{
									handCode = hand.getCard(i).getNumber() + hand.getCard(j).getNumber();
									handCode += table[k].getNumber();
									handCode += table[l].getNumber();
									handCode += table[m].getNumber();
									strength = Math.max(strength, HandEval.hand5Eval(handCode));
								}
					}
				}
			}
			
		}
		return rankToCategory(strength);
	}
	
	/**
	 * small method to convert the int 'rank' to a readable enum called HandCategory
	 */
	public HandEval.HandCategory rankToCategory(int rank) {
		return HandEval.HandCategory.values()[rank >> HandEval.VALUE_SHIFT];
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
