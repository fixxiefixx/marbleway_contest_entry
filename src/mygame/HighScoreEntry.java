/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author Finn
 */
public class HighScoreEntry implements Comparable<HighScoreEntry>{
    public int score=0;
    public String name=null;
    public HighScoreEntry(int score,String name)
    {
        this.score=score;
        this.name=name;
    }

    public int compareTo(HighScoreEntry o) {
        return o.score-score;
    }
    
    @Override
    public String toString()
    {
        String s=name; 
        String scores=Integer.toString(score);
        for(int i=0;i<25-name.length();i++)
        {
            s+=" ";
        }
        s+=scores;
        return s;
    }
}
