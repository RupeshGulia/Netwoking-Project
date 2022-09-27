package clientnwfilesharing;
import java.io.*;
import java.net.*;

public class Client extends Thread
{
  Socket skt;
  Client(String ip, int port) throws Exception
  {
    //create a Socket object
    //i.e. request a socket connection
    //with the said server
    skt = new Socket(ip, port);
    start();
  }
  
  //supporting method
  static String getString()
  {
    try
    {
      byte arr[] = new byte[512];
      //avoiding fetch/read of left out data
      System.in.skip(System.in.available());
      int n = System.in.read(arr);
      int deduct = "\n".length();
      return new String(arr, 0 , n-deduct);
    }
    catch(Exception ex)
    {
      return "";
    }
  }
  //executes concurrently
  public void run()
  {
    try
    {
      //query
      boolean flag = true;
      //fetch the socket streams
      //FYI:streams are for data exchange (i/o) 
      InputStream in;
      OutputStream out;
      in = skt.getInputStream();
      out = skt.getOutputStream();

      //for i/o of Java types 
      DataInputStream din = new DataInputStream(in);
      DataOutputStream dout = new DataOutputStream(out);
        
      String query, ch;
      int qty;
      int i;
      do
      {
        //menu
        System.out.println("1. Search ");
        System.out.println("2. Exit ");
        System.out.println("Enter Choice ");
        ch = getString();
        System.out.println("** " + ch );
        if(ch.equals("1"))
        {
          System.out.println("Enter Search String ");
          query = getString();//sorting
          dout.writeUTF("SEARCH");
          dout.writeUTF(query);
          qty = din.readInt();
        
          if(qty == 0)
          {
            System.out.println("No results for " + query);
          }
          else
          {
            System.out.println("Results: ");
            for(i =1 ; i <= qty; i++)
              System.out.println(i+")" + din.readUTF());
            
            System.out.println("Download Files? (y/n) ");
            ch = getString();
            if(ch.equalsIgnoreCase("y"))
            {
              System.out.println("Enter resultSeqNo,resultSeqNo,... for download (e.g. 1,3,4)");
              ch = getString();
              dout.writeUTF("DOWNLOAD");
              dout.writeUTF(ch);
              
              ch = din.readUTF();
              if(ch.equals("OK"))
              {
                long fileSize = din.readLong();
                
                byte buff[] = new byte[512];
                int n;
                long tot= 0;
                String trgtFile = System.getProperty("user.home") + "/downloads/myfiles.zip";
                FileOutputStream fout = new FileOutputStream(trgtFile);
                
                while(tot < fileSize)
                {
                  n = din.read(buff);
                  fout.write(buff, 0, n);
                  tot +=n;
                }
                fout.close();
                System.out.println("See: " + trgtFile);
              } 
              else if(din.equals("ERROR"))
                System.out.println("ERROR IN DOWNLOAD");
            }
            else
              dout.writeUTF("NO_DOWNLOAD");
          }
          flag = true;
        }
        else if(ch.equals("2"))
        {
          flag = false;
          dout.writeUTF("EXIT");
        }
        else
        {
          System.out.println("Wrong Choice");
          flag = true;
        }
      }while(flag);
      skt.close();
    }
    catch(Exception ex)
    {
      System.out.println("Err");
    }
  }
}
