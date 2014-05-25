/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;


/**
 *
 * @author Finn
 */
public class HighScore {
    public HighScoreEntry[] entries=new HighScoreEntry[20];
    
    public HighScore()
    {
        for(int i=0;i<entries.length;i++)
        {
            entries[i]=new HighScoreEntry(0,"<No Entry>");
        }
    }
    
    public int checkScorePos(int score)
    {
        int beipos=-1;
        for(int i=entries.length-1;i>=0;i--)
        {
            if(entries[i].score<score)
            {
                beipos=i;
            }else
            {
                break;
            }
        }
        return beipos;
    }
    
    
    
    public void addEntry(HighScoreEntry entry)
    {
        int beipos=checkScorePos(entry.score);
        if(beipos>=0)
        {
            for(int i=entries.length-2;i>=0 && i>=beipos;i--)
            {
                entries[i+1]=entries[i];
            }
            entries[beipos]=entry;
        }
    }
    
    @Override
    public String toString()
    {
        String s="Name                     Score\r\n";
        for(HighScoreEntry entry:entries)
        {
            s+=entry.toString()+"\r\n";
        }
        return s;
    }
    
    
}
