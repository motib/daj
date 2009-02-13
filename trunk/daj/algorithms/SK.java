//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Java Program for Suzuki-Kasami Algorithm for mutual exclusion
//  Frank Harvie.

package daj.algorithms;
import daj.*;

public class SK extends DistAlg
{

  public SK(int i, DistAlg[] da, Screen s) {
    super(i, da, s);
    init();
    initialize();
  }

  public void init()
  {
    for (int i = 0; i < number; i++) {
      RN[i] = 0;
      LN[i] = 0;
      TokenQ[i] = 5;
      seqNum = 0;
    }
    if (me==0) {
      state = enterCSState;
      gotToken = true;
    } else {
      state = requestCSState;
      gotToken = false;
    }
  }

  protected int constructButtons()
  {
    requestCSButtons      = addComponents("Request ",     REQUEST,      null, null);
    sendRequestToButtons  = addComponents("Send ",         SENDREQUEST,  null, null);
    waitingButtons         = addComponents("Waiting for ", WAITING,      null, null);
    enterCSButtons         = addComponents("Enter ",       ENTER,        null, null);
    inCSButtons           = addComponents(IN,              LEAVE,        null, null);
    sendTokenButtons       = addComponents("Send ",        SENDTOKEN,    null, null);
    toButtons              = addressButtons(null,           null, null);
    if (gotToken)
      return enterCSButtons;
    else
      return requestCSButtons;
  }

  protected String constructTitle1()
  {
    String s = " "+Screen.node[me]+": "+RN[me]+".    Got Token: ";
    if (gotToken)
      s = s + "YES";
    else
      s = s + "NO";
    return s;
  }

  protected String constructTitle2()
  {
      // Ville: moved the checking first so the string is only
      // created when needed.
    if (!gotToken) 
        return " ";
    String s = "Token Q: ";
    for (int i=0;i<4;i++)
      if (TokenQ[i]==5) s = s + ". ";
      else  s = s + TokenQ[i] +" ";
    s = s
        + "   LN :  J: " + LN[0]
        + "  Z: " + LN[1]
        + "  L: " + LN[2]
        + "  B: " + LN[3];
    return s;
  }

  protected String constructRow1(int g)
  {
    String s = Screen.node[g]+" : "+RN[g];
    return s;
  }

  protected String constructRow2(int g)
  {
    return " ";
  }

  protected boolean stateMachine(String b)
  {
    switch (state) {
      case requestCSState:
        if (b.equals(REQUEST)) {
          if (!gotToken) {
            seqNum++;
            RN[me]=seqNum;
            for (int i=0;i<number;i++)
              if (i!=me)
                needToSendReq[i]=true;
            changeState(sendRequestToState, requestCSButtons, sendRequestToButtons);
            return true;
          }
        } else return false;

      case sendRequestToState:
        if (b.equals(SENDREQUEST) && any(needToSendReq)) {
          changeState(toState, sendRequestToButtons, toButtons);
          return true;
        } else if (b.equals(SENDREQUEST)) {
          changeState(waitingState, sendRequestToButtons, waitingButtons);
          return true;
        }
        return false;

      case toState:
        toNode = FindNode(b);
        if (toNode==number)
          return false;
        else {
          needToSendReq[toNode] = false;
          if (!any(needToSendReq)) {
            if (!gotToken)
              changeState(waitingState, toButtons, waitingButtons);
            else changeState(inCSState, toButtons, inCSButtons);
          } else
            changeState(sendRequestToState, toButtons, sendRequestToButtons);
          send(requestMSG,toNode,me,seqNum);
          for (int i = 0; i < number; i++)
            send(TokenQMSG, toNode, i, TokenQ[i]);
          for (int i = 0; i < number; i++)
            send(LNMSG, toNode, i, LN[i]);
          return true;
        }

      case waitingState:
        if (b.equals(WAITING)) {
          if (gotToken)
            changeState(inCSState, waitingButtons, inCSButtons);
          return true;
        } else return false;

      case enterCSState:
        if (b.equals(ENTER)) {
          if (gotToken) {
            changeState(inCSState, enterCSButtons, inCSButtons);
            return true;
          } else if (!gotToken) {
            changeState(requestCSState, enterCSButtons, requestCSButtons);
            return true;
          }
        } else
          return false;

      case inCSState:
        if (b.equals(LEAVE)) {
          LN[me] = RN[me];
          for (int j = 0; j < number; j++)
          if (RN[j] == (LN[j] + 1))
            appendQ(j);
          if ((TokenQ[0]==5) /*&& (wantToEnter==false)*/) {
            changeState(enterCSState, inCSButtons, enterCSButtons);
            return true;
          } else {
            changeState(sendTokenState, inCSButtons, sendTokenButtons);
            return true;
          }
        } else return false;

      case sendTokenState:
        if (b.equals(SENDTOKEN)) {
          getNextSite();
          send(tokenMSG, next, 0, 0);
          for (int i = 0; i < number; i++)
            send(TokenQMSG, next, i, TokenQ[i]);
          for (int i = 0; i < number; i++)
            send(LNMSG, next, i, LN[i]);
          gotToken = false;
          changeState(requestCSState, sendTokenButtons, requestCSButtons);
          return true;
        } else return false;

      default:
        return false;
      }
  }

  private boolean any(boolean[] a) {
    for (int i=0;i<number;i++)
      if ((i!=me) && a[i]) {
        return true;
      }
    return false;
  }

  private boolean alreadyInQ(int j) {
    for (int i=0;i<number;i++)
      if (TokenQ[i]==j)
          inQ = true;
    if (inQ) {
      inQ = false;
      return true;
    } else
      return false;
  }

  private void appendQ(int j) {
    if (!alreadyInQ(j))
      for (int i=0;i<number;i++) {
        if (TokenQ[i]==5) {
          TokenQ[i]=j;
          break;
        }
      }
    return;
  }

  private int getNextSite() {
    next = TokenQ[0];
    for (int i = 0; i < number; i++)
      TempQ[i] = 5;
    for (int i = 0; i < (number - 1); i++)
      TempQ[i] = TokenQ[(i + 1)];
    TempQ[(number - 1)] = 5;
    for (int i = 0; i < number; i++)
      TokenQ[i] = TempQ[i];
    return next;
  }

  private boolean checkSeqNum(int sn, int from) {
    if (sn > RN[from])
      return true;
    else
      return false;
  }

  protected void receive(int msg, int fromWho, int itsSeqNum)
  {
    if (msg==tokenMSG) {
      if (state==sendRequestToState) {
        gotToken = true;
        return;
      } else {
        gotToken = true;
        changeState(inCSState, waitingButtons, inCSButtons);
        return;
      }
    }
    if (msg==requestMSG) {
      if (checkSeqNum(itsSeqNum,fromWho)) {
        RN[fromWho] = itsSeqNum;
        if ((RN[fromWho]==(LN[fromWho]+1)) && (state==enterCSState)) {
          gotToken = false;
          changeState(requestCSState, enterCSButtons, requestCSButtons);
          send(tokenMSG,fromWho,0,0);
          for (int i=0;i<number;i++)
            send(TokenQMSG,fromWho,i,TokenQ[i]);
          for (int i=0;i<number;i++)
          send(LNMSG,fromWho,i,LN[i]);
        } 
//        else
//          if ((RN[fromWho]==(LN[fromWho]+1)) && (state==inCSState)) {
//            wantToEnter = true;
//          }
      }
    }
    if (msg == TokenQMSG)
      TokenQ[fromWho] = itsSeqNum;
    if (msg == LNMSG)
      LN[fromWho] = itsSeqNum;
  }

  // Data structure for messages
  public int[] TokenQ            = new int[number];
  public int[] LN                = new int[number];

  private boolean[] needToSendReq = new boolean[number];
  private int[] RN                = new int[number];
//  private boolean wantToEnter      = false;
  private boolean gotToken        = false;
  private int seqNum              = 0;
  private boolean inQ              = false;
  private int[] TempQ              = new int[number];
  private int next = 0;

  // State machine
  private int toNode;
  private static final int tokenMSG      = 1;
  private static final int requestMSG    = 2;
  private static final int TokenQMSG    = 3;
  private static final int LNMSG        = 4;
  //private int state = requestCSState;
  private static final int requestCSState      = 0;
  private static final int sendRequestToState  = 1;
  private static final int toState            = 2;
  private static final int waitingState        = 3;
  private static final int enterCSState        = 4;
  private static final int inCSState          = 5;
  private static final int sendTokenState      = 6;

  // Components of user interface

  private int
    requestCSButtons, sendRequestToButtons, waitingButtons, enterCSButtons,
    sendTokenButtons, toButtons, inCSButtons;

  private static final String
    REQUEST     = "CS",
    SENDREQUEST  = "Request",
    WAITING      = "Token",
    ENTER        = "CS",
    IN          = " You are in the critical section. Leave ",
    LEAVE        = "Now",
    SENDTOKEN    = "Token";
}
