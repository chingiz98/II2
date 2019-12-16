package Tickets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicketGui extends JFrame
{
    private Tickets myAgent;

    TicketGui(Tickets q)
    {
        super(q.getLocalName());

        JPanel p = new JPanel();
        JLabel txt = new JLabel("Кол-во билетов:");
        JTextField count = new JTextField(12);
        p.setLayout(new GridLayout(1,2));
        p.add(txt);
        p.add(count);
        getContentPane().add(p, BorderLayout.CENTER);
        myAgent = q;
        JButton addButton = new JButton("Ok");
        addButton.addActionListener(e -> myAgent.getTickets(Integer.parseInt(count.getText())));
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );
        setResizable(true);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
