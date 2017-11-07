
package table;

import java.net.SocketException;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import allclasses.*;
import allclasses.Order;
import allclasses.RestaurantItem;
import allclasses.ServerSentMasterList;


public class Table
{ 
    private final int ID = 1; // the table's id
    private Socket TableSkt = null; // the table's socket
    private ObjectInputStream ObjIn = null; // stream used to read in objects
    private ObjectOutputStream ObjOut = null; // stream used to output objects
    private boolean Connected = false;
    
    private ServerSentMasterList SentMenu;
    private MasterFoodItemList Menu;
    private Order Order = null; // an array list containing foods, drinks, and merch ordered by the customer
    
    public Table()
    {
           Order = new Order();
    }
    
    public void Handshake()
    {
        try
        {
            String category = "Table@1"; // a string used by the server to determine what action to take
            TableSkt = new Socket("localHost", 5555); // create a new socket for the customer
            System.out.println("connected."); // test
            Connected = true; // the user is now connected to the server
            
            // create an object input and output stream after connecting to the server
            ObjOut = new ObjectOutputStream(TableSkt.getOutputStream());
            ObjOut.flush();
            ObjIn = new ObjectInputStream(TableSkt.getInputStream());
            
            // launch a thread dedicated to reading from the server
            Thread Listening = new Thread(new ListeningThread()); 
            Listening.start();
            
            // send a message to the server so that it launches a specific thread
            System.out.println("Sending message."); // test
            ObjOut.writeUTF(category);
            ObjOut.flush();
            
            Thread.sleep(100); // test may be part of final build allow server to catch up
        }
        catch(SocketException se)
        {
             System.exit(0);
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            System.out.println("Error connecting to server" + e);
        }
    }
    
    // this class is a thread that will read input sent from the server
    public class ListeningThread implements Runnable
    {
        @Override
        public void run()
        {
            String Message; // the message from the server
                       
            try
            {
                System.out.println("Getting Menu");
                SentMenu = (ServerSentMasterList)ObjIn.readObject();
                Menu = new MasterFoodItemList(SentMenu.totalList);
                
                for (int i = 0; i < Menu.drinks.size(); i++)
            {
                System.out.println(Menu.drinks.get(i).GetName());
            }
            System.out.println();
            
            for (int i = 0; i < Menu.appetizers.size(); i++)
            {
                System.out.println(Menu.appetizers.get(i).GetName());
            }
            System.out.println();
            
            for (int i = 0; i < Menu.entries.size(); i++)
            {
                System.out.println(Menu.entries.get(i).GetName());
            }
            System.out.println();
            
            for (int i = 0; i < Menu.desserts.size(); i++)
            {
                System.out.println(Menu.desserts.get(i).GetName());
            }
            System.out.println();
                
                while((Message = ObjIn.readUTF()) != null)
                {
                    System.out.println(Message); // test
                    if(Message.equals("Modify")) // if the order was modified by the waiter
                    {
                        Order ModifiedOrder = (Order)ObjIn.readObject();
                        Order = ModifiedOrder;
                        
                        
                        System.out.println("Checking contents of modified Order.");
                        for(int i = 0; i < Order.GetOrderSize(); i++)
                        {
                            System.out.println(Order.GetItem(i).GetName());
                            for(int j = 0; j < (((Food)Order.GetItem(i)).getIngrediantArraySize()); j++)
                            {
                                System.out.println(((Food)Order.GetItem(i)).GetIngredients(j));
                            }
                        }
                        
                        // clear the order and read the new order
                    }
                    else if(Message.equals("Dismiss")) // if the waiter cleared the table
                    {
                        // remove all necessary values, i.e. price, gamepin
                        // send a message to remove waiter
                    }
                    else if(Message.equals("Shutdown")) // test
                    {
                        break;
                    }
                }
                Connected = false; // test closes the infinite loop in main
            }
            catch(Exception e)
            {
                System.out.println("Error receiving information from server.");
            }
        }
    }
    
    // this function is used to add an item to table's order
    public void AddToOrder(Food newItem)
    {
        Order.AddToOrder(newItem); // call the add to order in the list class
    }
    
    // this function is used to send a table's order to the server
    public void SendOrder()
    {
        try
        {
            ObjOut.writeUTF("Send"); // send a message to the server
            ObjOut.flush();
            
            Thread.sleep(100);
            
            ObjOut.writeObject(Order); // send the object to the server
            ObjOut.flush();
                      
            Thread.sleep(100);
        }
        catch(Exception e)
        {
            System.out.println("Error sending order." + e);
        }
    }
    
    public void RequestHelp()
    {
        try
        {
           // System.out.println("Sending help."); // test
            ObjOut.writeUTF("Help@"+Integer.toString(ID)); // Send the request to the server
            ObjOut.flush();
        }
        catch(Exception e)
        {
            System.out.println("Error Requesting Help."+ e);
        }
    }
    
    public void CloseConnections()
    {
        try
        {
            // close in streams and the socket
            ObjIn.close();
            ObjOut.close();
            TableSkt.close();
        }
        catch(IOException e)
        {
            System.out.println("Error closing Streams and sockets");
        }
        
    }
    /* not needed??
    public void SetTableNum(int i)
    {
        this.Order.SetTableNum(i);
    }*/
    public static void main(String argv[])
    {
        // test cases
        Table table1 = new Table();
        
        Food temp = new Food("Diggity Dog", 5.00, true, false);
        temp.SetIngredients("test");
        temp.SetDescription("test");
            
        Food temp1 = new Food("Hamburglar", 7.00, true, false);
        temp1.SetIngredients("test2");
        temp1.SetDescription("test3");
            
        Food temp3 = new Food("Squeezed Cow", 5.00, true, false);
        temp3.SetIngredients("Calcium");
        temp3.SetDescription("Freshly Squeezed!");
        
        table1.AddToOrder(temp);
        table1.AddToOrder(temp1);
        table1.AddToOrder(temp3);
        //table1.SetTableNum(1);
        
        System.out.println("Attempting to connect");
        
        table1.Handshake();
        table1.SendOrder();
        table1.RequestHelp();
        
        // test loop
        while(table1.Connected)
        {
            
        };
        table1.CloseConnections();
        
        System.out.println("finished");
    }
}
