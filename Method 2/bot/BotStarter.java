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

import poker.Card;
import poker.HandOmaha;
import poker.PokerMove;
import java.util.Random;
import com.stevebrecher.HandEval;
import java.util.*;
import java.util.LinkedList;

/**
 * This class is the brains of your bot. Make your calculations here and return the best move with GetMove
 */
public class BotStarter implements Bot {
	
     
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
		HandOmaha hand = state.getHand();
		Card[] table=state.getTable();
		/*HandEval.HandCategory handCategory1=getHandCategory(hand, table);*/
		int handCategory1=getHandCategorynum(hand,table);
		/*String handCategory11 = handCategory1.toString();*/
		System.err.printf("my hand is %s, opponent action is %s, pot: %d,rationumber: %d%n", handCategory1, state.getOpponentAction(), state.getPot(),state.raisenumber);
	    String chose;
	    /*if(table.length<3)
	    {chose=Firstbet(hand,state);
	    state.raisenumber=0;
	    }
	    else
	    {*/
	    if(state.getOpponentAction()!=null)
	    {
	    if(state.getOpponentAction().getAction().equals("raise"))
	    {state.raisenumber=state.raisenumber+1;}
	    else	
	    {
	    	if(state.raisenumber>0)
	    	{
	    	state.raisenumber=state.raisenumber-1;
	    	}
	    }
	    }
	    if(state.trialnumber==0)
	    {state.initialpot=state.getPot()+state.getAmountToCall();}
	    chose=AImove(hand,table,state);
	    state.trialnumber=state.trialnumber+1;
	    if( chose == "raise") {
	    	// state.raisenumber=state.raisenumber+1;
			return new PokerMove(state.getMyName(), "raise",state.getOpponentStack()-(state.getAmountToCall()+state.getPot()-state.initialpot)/2);
		} else if( chose == "call") {
			if(state.raisenumber>0)
			{
			state.raisenumber=state.raisenumber-1;
			}
			return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
		} else {
			if(state.raisenumber>0)
	    	{	
	    	state.raisenumber=state.raisenumber-1;
	    	}
			return new PokerMove(state.getMyName(), "check", 0);}
	    
	    
	}
	public String AImove(HandOmaha hand, Card[] table,BotState state)
	{
		
	  
	   
	    Card[] expecttable=new Card[5];
		for (int k=0;k<table.length;k++)
		{
			expecttable[k]=table[k];
		}
	int win_check=0;
	int tie_check=0;
	int lose_check=0;
	for (int j=0;j<20000;j++){
	ArrayList<String> cardordinallist1=new ArrayList();
    	for(int i=0;i<52;i++)
	    {
    		Card cardss=new Card(i);
    		cardordinallist1.add(cardss.toString());
	    }
	   
	    for(int i=0;i<4;i++)
	    {
	    	cardordinallist1.remove(hand.getCard(i).toString());
	    	}
		for (int k=0;k<table.length;k++)
		{
			cardordinallist1.remove(table[k].toString());
		}
	Card[] opponentcards=new Card[4];
	for(int i=0;i<4;i++){
	Random randomGenerator=new Random();
	int new_card=randomGenerator.nextInt(cardordinallist1.size());	
	opponentcards[i]=Card.getCard(cardordinallist1.get(new_card));
	cardordinallist1.remove(new_card);
	}
	HandOmaha opponenthand = new HandOmaha(opponentcards[0], opponentcards[1],opponentcards[2], opponentcards[3]);
	
	if(table.length<5)
	{
	for(int k=table.length;k<5;k++)
	{
		Random randomGenerator=new Random();
		int new_card=randomGenerator.nextInt(cardordinallist1.size());	
		expecttable[k]=Card.getCard(cardordinallist1.get(new_card));
		cardordinallist1.remove(new_card);	
	}
	}
	if(getHandCategorynum(hand, expecttable)>getHandCategorynum(opponenthand, expecttable))
	{win_check=win_check+1;}
	else if(getHandCategorynum(hand, expecttable)<getHandCategorynum(opponenthand, expecttable))
	{lose_check=lose_check+1;}
	else{tie_check=tie_check+1;}
	}
	double ratio=((double)win_check)/((double)win_check+(double)lose_check);
	double basicratio=(2.0*(double) state.getAmountToCall())/((double) state.getPot()+state.getAmountToCall());
	if(table.length==0)
	{basicratio=Math.min(0.5, basicratio);}
	double basicratio1=(2.0*((double)state.getPot()-(double) state.getAmountToCall()))/(3.0*(double) state.getPot()-state.getAmountToCall());
	if(table.length==0)
	{basicratio=Math.min(0.5, basicratio1);}
	if(state.getOpponentAction()!=null)
	{
	if(state.getOpponentAction().getAction().equals("raise"))
	{
		double raisenum=(double) state.raisenumber;
		double multiply=(double) Math.pow(0.5, raisenum+1);
		if((1.0-ratio)<(1.0-basicratio1)*multiply)
		{return "raise";}
		else if(ratio>basicratio)
	{      
			return "call";}
	else
	{return "check";}	
	}
	}
	double raisenum= (double) state.raisenumber;
	double multiply=(double) Math.pow(0.5, raisenum+1);
	if((1.0-ratio)<(1.0-basicratio1)*multiply)
	{return "raise";}
	else if(ratio<((double) state.getAmountToCall())/((double) state.getAmountToCall()+(double) state.getPot()))
	{
		return "check";}
	else
	{return "call";}	
	}
    
	
		// Get the ordinal values of the cards in your hand
		/*int[] ordinalHand = {
			hand.getCard(0).getHeight().ordinal(),
			hand.getCard(1).getHeight().ordinal(),
			hand.getCard(2).getHeight().ordinal(),
			hand.getCard(3).getHeight().ordinal()
		};
		
		// Get the average ordinal value
		double averageOrdinalValue = 0;
		for(int i=0; i<ordinalHand.length; i++) {
			averageOrdinalValue += ordinalHand[i];
		}
		averageOrdinalValue /= ordinalHand.length;*/
		
		// Return the appropriate move according to our amazing strategy
		/*if( handCategory1 == HandEval.HandCategory.NO_PAIR) {
			return new PokerMove(state.getMyName(), "raise", 2*state.getBigBlind());
		} else if( handCategory1 == HandEval.HandCategory.PAIR ) {
			return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
		} else {
			return new PokerMove(state.getMyName(), "check", 0);
		}*/
	public String Firstbet(HandOmaha hand,BotState state)
	{
		int[] cardHeight={
				hand.getCard(0).getHeight().ordinal(),
				hand.getCard(1).getHeight().ordinal(),
				hand.getCard(2).getHeight().ordinal(),
				hand.getCard(3).getHeight().ordinal()
		};
		int[] cardSuit={
				hand.getCard(0).getSuit().ordinal(),
				hand.getCard(1).getSuit().ordinal(),
				hand.getCard(2).getSuit().ordinal(),
				hand.getCard(3).getSuit().ordinal()			
		};
		Map<Integer,Integer> handMapHeight=new HashMap<Integer,Integer>();
		/*for(int i=0;i<12;i++)
		{handMapHeight.put(i, 0);}*/
		
	    for(int i=0;i<4;i++)
	        if(handMapHeight.containsKey(cardHeight[i])){
	        Integer label_value=handMapHeight.get(cardHeight[i]);
	        label_value=label_value+1;
	        handMapHeight.put(cardHeight[i], label_value);		
	        }else
	        {
	        handMapHeight.put(cardHeight[i], 1);	
	        }
	  
		   if(handMapHeight.containsValue(3)||handMapHeight.containsValue(4))
		   {return "check";}
		 if(state.trialnumber>=2)
		 {return "call";}
	   
		   
	   if(handMapHeight.containsKey(12)&&handMapHeight.get(12)==2){
		   return "raise";
	   }
	   else if(handMapHeight.containsKey(11)&&handMapHeight.get(11)==2)
	   {
		int check_exist=0;
		for(int i=0;i<8;i++)
		{
		if(handMapHeight.containsKey(i))
		{check_exist=1;}
		}
		if(check_exist==0)
		{return "raise";}
	   }
	   else if(handMapHeight.containsKey(9)&&handMapHeight.containsKey(8)&&handMapHeight.containsKey(7)&&handMapHeight.containsKey(6))
	   {return "raise";}
	   else  if(handMapHeight.containsKey(10)&&handMapHeight.get(10)==2)
	   {
		   int check_exist=0;
			for(int i=0;i<8;i++)
			{
			if(handMapHeight.containsKey(i))
			{check_exist=1;}
			}
			if(check_exist==0)
			{return "raise";}
			if((handMapHeight.containsKey(7)&&handMapHeight.containsKey(9))||(handMapHeight.containsKey(7)&&handMapHeight.get(7)==2))
			{return "raise";}		
	   }
	   else if(handMapHeight.containsKey(9)&&handMapHeight.get(9)==2)
	   {
		   if(handMapHeight.containsKey(8)&&handMapHeight.containsKey(7))
		   {return "raise";}
		   if(handMapHeight.containsKey(8)&&handMapHeight.get(8)==2)
		   {return "raise";}
	   }
	  
	   return "call";
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
	public int getHandCategorynum(HandOmaha hand, Card[] table) {
		int strength = 0;
		
		// Try all possible combinations of 2 out of 4 cards for what we have in our hand (6 possibilities)
		for(int i=0; i<hand.getNumberOfCards()-1; i++) {
			for(int j=i+1; j<hand.getNumberOfCards(); j++) {
				
				/*if( table == null || table.length == 0 ) { // The table is empty, so we just check what we have in our hand and a pair is the best we can do
					if( hand.getCard(i).getHeight() == hand.getCard(j).getHeight() ) { // If two cards have the same height:
						return HandEval.HandCategory.PAIR; // We found a pair; return that we have a pair
					}
					else if ( i == hand.getNumberOfCards() - 2 && j == hand.getNumberOfCards() - 1 ) { // Last pair of cards
						return HandEval.HandCategory.NO_PAIR; // If we reach this we didn't find a pair, so return NO_PAIR
					}
					
				}else*/ 
				{ // There are cards on the table
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
		/*return rankToCategory(strength);*/
		return strength;
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
