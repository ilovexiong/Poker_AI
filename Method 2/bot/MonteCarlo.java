package bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.stevebrecher.HandEval;

import poker.Card;
import poker.HandOmaha;
import poker.PokerMove;

public class MonteCarlo {
    private int myStack;
    private int oppStack;
    private int tempPot;
    private ArrayList<Card> tempTable;
    private PokerMove simMove;
    private String oppLastMove;
    private String lastMove;
    private boolean onButton;
    private int toCall;
    private int[] monteCarloChoices = new int[3];    //Indexes are {check, call, raise}
    private String firstMove;
    private ArrayList<Card> deck;
    private boolean first;
    private HandOmaha myHand;
    private int result;
    private int raiseAmt;
    private Card[] tableArray;
    
    Random rand = new Random();
    
    public MonteCarlo(ArrayList<Card> tempDeck, BotState state, String firstMove) {
        myStack = state.getmyStack();
        oppStack = state.getOpponentStack();
        tempPot = state.getPot();
        tableArray = state.getTable();
        tempTable = new ArrayList<Card>(Arrays.asList(tableArray));
        simMove = state.getOpponentAction();
        lastMove = "";
        onButton = state.onButton();
        this.firstMove = firstMove;
        deck = tempDeck;
        first = true;
        myHand = state.getHand();
        raiseAmt = state.getAmountToCall();
        
        if (simMove != null) {
            oppLastMove = simMove.getAction();
            toCall = simMove.getAmount();
        } else {
            oppLastMove = null;
            toCall = 0;
        }
        
    }
    
    public void runSim() {
        if (oppLastMove == null) {
            switch (firstMove) {
                case "check":
                    simCheck("me", "opp");
                    break;
                case "raise":
                    int amt = getRaiseAmt("me");
                    simRaise("me", "opp", amt);
                    break;
            }
        } else if (onButton && first) {            //We are on button and this is the first move of the hand
            first = false;
            if (oppLastMove.equals("check")) {
                switch (firstMove) {
                    case "check":
                        addCard();
                        break;
                    case "raise":
                        int amt = getRaiseAmt("me");
                        simRaise("me", "opp", amt);
                        break;
                }
            } else {                        //Opponent's last move = raise
                switch (firstMove) {
                case "check":
                    calcMoney("opp");
                    break;
                case "raise":
                    if (updatePot(raiseAmt, "me")) {
                        int amt = getRaiseAmt("me");
                        simRaise("me", "opp", amt);
                    } else {
                        for (int i = 0; i < 6-tempTable.size();i++)
                        {
                            addCard();
                        }
                    }
                    break;
                case "call":
                    updatePot(raiseAmt, "me");
                    addCard();
            }
            }
        } else if (onButton && !first) {
            int temp = rand.nextInt(2);
            if (temp == 0) {                //Opponent checks
                simCheck("opp", "me");
            } else {                        //Opponent raises
                int oppAmt = getRaiseAmt("opp"); // newly added, it was looking at raise amount before
                simRaise("opp","me",oppAmt);
            }
        } else {                            //onButton  = false
            int tempRand = rand.nextInt(2);
            if (tempRand == 0) {            //We check
                simCheck("me", "opp");
            } else {                        //We raise
                int amt = getRaiseAmt("me");
                if (updatePot(amt, "opp")) {
                    simRaise("me", "opp", amt);
                } else {
                    for (int i = 0; i < 6-tempTable.size();i++)
                    {
                        addCard();
                    }
                }
            }
        }
    }

    /*
     * Player 1 checked. Player 2's turn to move.
     */
    public void simCheck(String player1, String player2) {
        ////System.out.println(player1 + " check");
        int tempRand = rand.nextInt(2);
        if (tempRand == 0) {            //Player checks
            addCard();
        } else {                        //Player raises
            int amt = getRaiseAmt(player2);
            simRaise(player2, player1, amt);
        }
    }

    /*
     * Player 1 raised. Player 2's turn to move.
     */
    public void simRaise(String player1, String player2, int raise) {
        ////System.out.println(player1 + " raises heyyo " + raise);
        int tempRand = rand.nextInt(3);
        if (tempRand == 0) {            //Player folds
            ////System.out.println(player2 + " folds");
            calcMoney(player1);
        } else if (tempRand == 1){        //Player calls
            ////System.out.println(player2 + " calls");
            if (updatePot(raise, player2)) {
                addCard();
            } else {
                for (int i = 0; i < 6-tempTable.size();i++)
                {
                //while (tempTable.size() <= 5) {
                    addCard();
                }
            }
        } else {                        //Player raises    
            if (updatePot(raise, player2)) {
                int amt = getRaiseAmt(player2);
                ////System.out.println(player2 + " raises " + amt);
                simRaise(player2, player1, amt);
            } else {
                ////System.out.println("someone is all in");
                for (int i = 0; i < 6-tempTable.size();i++)
                {
                //while (tempTable.size() <= 5) {
                    addCard();
                }
            }        
        }
    }

    public void addCard() {
        //System.out.println("table size is: " + tempTable.size());
        if (tempTable.size() < 5) {
            int tempInt = rand.nextInt(deck.size());
            Card newCard = deck.get(tempInt);
            deck.remove(newCard);
            tempTable.add(newCard);
            System.out.println("table =" + tempTable.toString());
            runSim();
        } else {
            checkHands();
        }
    }
    
    /*
     * Player 2 is the player who is responding to the raise.
     */
    public boolean updatePot(int raiseAmt, String player2) {
        int player1Stack;
        int player2Stack;
        boolean notAllIn = true;

        if (player2.equals("me")) {
            player1Stack = oppStack;
            player2Stack = myStack;
        } else {
            player1Stack = myStack;
            player2Stack = oppStack;
        }
        
        if ((player2Stack - raiseAmt) <= 0) {      
            player2Stack = 0;
            player1Stack -= player2Stack;
            tempPot += 2 * player2Stack;
            notAllIn = false;
        } else {
            player2Stack -= raiseAmt;
            player1Stack -= raiseAmt;
            tempPot += 2 * raiseAmt;
        }
        
        if (player2.equals("me")) {
            oppStack = player1Stack;
            myStack = player2Stack;
        } else {
            myStack = player1Stack;
            oppStack = player2Stack;
        }
        System.out.println("myStack = " + myStack + " oppStack = " + oppStack + " pot = " + tempPot);
        return notAllIn;
    }
    
    public int getRaiseAmt(String player1) {
        int tempStack;
        if (player1.equals("me")) {
            tempStack = myStack;
        } else {
            tempStack = oppStack;
        }
        ////System.out.println("tempStack = " + tempStack);
        int temp = rand.nextInt(4);
        int amt = 0;
        switch (temp) {
        case 0:
            amt = tempPot/4;
            if ((tempStack - amt) <= 0) {
                amt = tempStack;
            }
            break;
        case 1:
            amt = tempPot/2;
            if ((tempStack - amt) <= 0) {
                amt = tempStack;
            }
            break;
        case 2:
            amt = tempPot * (3/4);
            if ((tempStack - amt) <= 0) {
                amt = tempStack;
            }
            break;
        case 3:
            amt = tempPot;
            if ((tempStack - amt) <= 0) {
                amt = tempStack;
            }
            break;
        }
        
        if (player1.equals("me")) {
            myStack = tempStack;
        } else {
            oppStack = tempStack;
        }
        
        return amt;
    }
    
    public void checkHands() {
        Card[] oppHandArray = new Card[4];
        for (int i = 0; i < 4; i++) {
               int numInDeck = deck.size();
            int deckChoice = rand.nextInt(numInDeck);
            //System.out.println("numInDeck: " + numInDeck);
            Card pickedCard = deck.get(deckChoice);
            deck.remove(pickedCard);
            oppHandArray[i] = pickedCard;
        }
        Card[] tableArray = new Card[4];
        HandOmaha opponentHand = new HandOmaha(oppHandArray[0],oppHandArray[1],oppHandArray[2],oppHandArray[3]);
        //HandEval.HandCategory myStrength = getHandCategory(myHand, tempTable.toArray(tableArray));
        //HandEval.HandCategory oppStrength = getHandCategory(opponentHand, tempTable.toArray(tableArray));
        
        
        int myStrength = getHandCategory(myHand, tempTable.toArray(tableArray));
        int oppStrength = getHandCategory(opponentHand, tempTable.toArray(tableArray));
      
        System.out.println("opponent Hand = " + opponentHand);
        System.out.println("opponent Hand strength = " + oppStrength);
        System.out.println("my hand strength = " + myStrength);
        
        if (myStrength > oppStrength)
        {
            calcMoney("me");
        }
        else if (myStrength < oppStrength)
        {
            calcMoney("opp");
        }
        else
        {
            calcMoney("tie");
        }
    }
    
    public void calcMoney(String winner) {
        if (winner.equals("me")) {
            result = tempPot;
        } else if (winner.equals("opp")) {
            result = -1 * tempPot;
        } else {
            result = tempPot/2;
        }
        System.out.println(">>>>>>result = "+ result);
    }
    
    //public HandEval.HandCategory getHandCategory(HandOmaha hand, Card[] table) {
    public int getHandCategory(HandOmaha hand, Card[] table) {
        int strength = 0;
        
        // Try all possible combinations of 2 out of 4 cards for what we have in our hand (6 possibilities)
        for(int i=0; i<hand.getNumberOfCards()-1; i++) {
            for(int j=i+1; j<hand.getNumberOfCards(); j++) {
                
                /*
                if( table == null || table.length == 0 ) { // The table is empty, so we just check what we have in our hand and a pair is the best we can do
                    if( hand.getCard(i).getHeight() == hand.getCard(j).getHeight() ) { // If two cards have the same height:
                        return HandEval.HandCategory.PAIR; // We found a pair; return that we have a pair
                    }
                    else if ( i == hand.getNumberOfCards() - 2 && j == hand.getNumberOfCards() - 1 ) { // Last pair of cards
                        return HandEval.HandCategory.NO_PAIR; // If we reach this we didn't find a pair, so return NO_PAIR
                    }
                */
                    
                //} else { // There are cards on the table
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
                //}
            }
            
        }
        return strength;
        //return rankToCategory(strength);
    }
    
    /**
     * small method to convert the int 'rank' to a readable enum called HandCategory
     */
    public HandEval.HandCategory rankToCategory(int rank) {
        return HandEval.HandCategory.values()[rank >> HandEval.VALUE_SHIFT];
    }
  
    public int getMyStack() {
        return myStack;
    }

    public void setMyStack(int myStack) {
        this.myStack = myStack;
    }

    public int getOppStack() {
        return oppStack;
    }

    public void setOppStack(int oppStack) {
        this.oppStack = oppStack;
    }

    public int getTempPot() {
        return tempPot;
    }

    public void setTempPot(int tempPot) {
        this.tempPot = tempPot;
    }

    public ArrayList<Card> getTempTable() {
        return tempTable;
    }

    public void setTempTable(ArrayList<Card> tempTable) {
        this.tempTable = tempTable;
    }

    public PokerMove getSimMove() {
        return simMove;
    }

    public void setSimMove(PokerMove simMove) {
        this.simMove = simMove;
    }

    public String getLastMove() {
        return lastMove;
    }

    public void setLastMove(String lastMove) {
        this.lastMove = lastMove;
    }

    public int getToCall() {
        return toCall;
    }

    public void setToCall(int toCall) {
        this.toCall = toCall;
    }

    public int[] getMonteCarloChoices() {
        return monteCarloChoices;
    }

    public void setMonteCarloChoices(int[] monteCarloChoices) {
        this.monteCarloChoices = monteCarloChoices;
    }

    public String getOppLastMove() {
        return oppLastMove;
    }

    public void setOppLastMove(String oppLastMove) {
        this.oppLastMove = oppLastMove;
    }

    public String getFirstMove() {
        return firstMove;
    }

    public void setFirstMove(String firstMove) {
        this.firstMove = firstMove;
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    public int getResult() {
        return result;
    }
    
}
