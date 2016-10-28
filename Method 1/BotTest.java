package bot;

import java.util.ArrayList;
import java.util.Arrays;

import poker.*;

public class BotTest {
	static BotState state = new BotState();
	
	
	public static ArrayList<Card> createDeck (HandOmaha hand, Card[] table) {
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
	
	public static void main(String[] args) {
		Card a = new Card(44);
		Card b = new Card(18);
		Card c = new Card(12);
		Card d = new Card(49);
		Card[] table = {new Card(3), new Card(15), new Card(38), new Card(31)};
		HandOmaha testHand = new HandOmaha(a,b,c,d);
		ArrayList<Card> temp = createDeck(testHand, table);
		System.out.println("Hand: " + a + " " + b + " " + c + " " + d);
		System.out.println("Table: " + Arrays.toString(table));
		for (int i = 0; i < temp.size(); i++) {
			//System.out.print(temp.get(i) + " ");
		}
		
		//state.updateSetting("startingStack", "2000");
		//state.updateMatch("onButton", "true");
		
		
		state.setMyStack(1920);
		state.setOpponentStack(2000);
		state.setPot(80);
		state.setAmountToCall(0);
		state.setOnButton(true);
		PokerMove opponentMove = new PokerMove("oppBot", "check", 0);
		state.setOpponentMove(opponentMove);
		state.setTable(table);
		state.setHand(testHand);
		state.setBigBlind(20);
		int counter = 0;
		
		final long startTime = System.currentTimeMillis();
		int wins = 0;
		
		for (int i = 0; i < 20000; i ++) {
			ArrayList<Card> realDeck = new ArrayList<Card>();
			realDeck.addAll(temp);
			MonteCarlo simCheck = new MonteCarlo(realDeck, state, "check");
			//System.out.println("myStack = " + state.getmyStack());
			simCheck.runSim();
			counter += simCheck.getResult();
			wins += simCheck.getWinCounter();
		}
		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime) );
		System.out.println("Counter: " + counter);
		System.out.println("Wins: " + wins);
	}
}
