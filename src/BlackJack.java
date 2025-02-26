import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)){
                if (value.equals("A"))   // A
                    return 11;
                return 10;          // J Q K
            }
            return Integer.parseInt(value); // 2-10
        }

        public boolean isAce() {
            return (value == "A");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); // shuffle deck

    Font countFont = new Font("Arial", Font.PLAIN, 20);

    //Dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    //card size
    int cardWidth = 110;    // 1/1.4 ratio
    int cardHeight = 154;

    //score
    int curScore = 0;
    int highScore = 0;

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                g.setFont(countFont);
                g.setColor(Color.white);

                //scores
                g.drawString("High Score: ", 430, 235);
                g.drawString(String.valueOf(highScore),550,235);
                g.drawString("Current Score: ", 403, 265);
                g.drawString(String.valueOf(curScore),550,265);

                //draw hidden card
                Image hiddenCardImage = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImage, 20, 20, cardWidth, cardHeight, null);

                //draw dealer's hand
                for (int i =0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight,null);
                }

                //draw player's hand
                for (int i =0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight,null);
                    g.drawString(String.valueOf(getReducedPlayerSum()), 280, 510);
                }

                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = getReducedPlayerSum();
                    System.out.println("STAY:");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    String message = "";
                    if (playerSum > 21) {
                        message = "You Lose!";
                    }
                    else if (dealerSum > 21) {
                        message = "You Win!";
                    }
                    else if (playerSum == dealerSum) {
                        message = "Tie!";
                    }
                    else if (playerSum > dealerSum) {
                        message = "You Win!";
                    }
                    else if (playerSum < dealerSum) {
                        message = "You Lose!";
                    }

                    g.drawString(message,220,250);
                    g.drawString(String.valueOf(playerSum), 280, 510);
                    g.drawString(String.valueOf(dealerSum), 280, 210);
                }
                else {
                    g.drawString(String.valueOf(dealerSum - hiddenCard.getValue()), 280, 210);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //buttons
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton redealButton = new JButton("Redeal");

    BlackJack() {
        startGame();

        //window initialization
        frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //background
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53,101,77));
        frame.add(gamePanel);

        //buttons
        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        stayButton.setFocusable(false);
        buttonPanel.add(redealButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        //button implimentations
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ('H' == hitOrStand())
                    increaseScore();
                else
                    curScore = 0;

                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce()? 1: 0;
                playerHand.add(card);
                if (getReducedPlayerSum() > 21) {
                    hitButton.setEnabled(false);
                }

                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hitButton.isEnabled() && hitOrStand() == 'S')
                    increaseScore();
                else if (hitButton.isEnabled() && hitOrStand() != 'S')
                    curScore = 0;

                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce()? 1: 0;
                    dealerHand.add(card);
                }

                gamePanel.repaint();
            }
        });

        redealButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
                gamePanel.repaint();
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {
        //deck
        buildDeck();
        shuffleDeck();

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1: 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1: 0;
        dealerHand.add(card);

        System.out.println("DEALER:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1: 0;
            playerHand.add(card);
        }

        System.out.println("PLAYER:");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] types = {"C","D","H","S"};

        for (int i =0; i < types.length; i++) {
            for (int j =0; j < values.length; j++) {
                Card card = new Card(values[j],types[i]);
                deck.add(card);
            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i =0 ; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE:");
        System.out.println(deck);
    }

    public int getReducedPlayerSum() {
        int tempSum = playerSum;
        int tempAceCount = playerAceCount;

        while (tempSum > 21 && tempAceCount > 0) {
            tempSum -= 10;
            tempAceCount -= 1;
        }
        return tempSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    public char hitOrStand() {
        if(playerAceCount > 0 && playerSum < 21) { //"Soft" hands containing ace and less than 21
            if (playerSum >= 19)
                return 'S';
            else if (playerSum <= 17)
                return 'H';
            else if (playerSum == 18 && dealerHand.get(0).getValue() >= 9)
                return 'H';
            return 'S';
        }
        else { //"Hard" hands
            if (getReducedPlayerSum() <= 11) {
                System.out.println("Sum < 11");
                return 'H';
            }
            else if (getReducedPlayerSum() >= 17)
                return 'S';
            else if (dealerHand.get(0).getValue() >= 7)
                return 'H';
            else if (getReducedPlayerSum() == 12 && dealerHand.get(0).getValue() <= 3)
                return 'H';
            return 'S';
        }
    }

    public void increaseScore() {
        ++curScore;
        if (curScore > highScore)
            highScore = curScore;
    }
}