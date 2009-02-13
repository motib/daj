//
//	Distributed Algorithms in Java
//	See file copyright.txt for credits and copyright.
//

//	Maekawa Subset for Mutual Exclusion
//	 Derick Burger and Darrell Newing

package daj.algorithms;
import daj.*;

public class MA extends DistAlg {
	public MA(int i, DistAlg[] da, Screen s) {
		super(i, da, s);
		init();
		initialize();
	}

	// Initialize data structures.
	protected void init() {
		for (int i = 0; i < number; i++) {
			deferred[i]	= false;
			needToSendRequest[i] = false;
			needToSendReply[i] = false;
			replyReceived[i] = false;
		}
		state = enterState;
		wantToEnter = false;
		released = true;
	}

	protected int constructButtons() {
		requestButtons = addComponents("", REQUEST, REPLY, null);
		releaseButtons = addComponents(RELEASELABEL, RELEASE, null, null);
		sendButtons    = addComponents("", SENDREQUEST, SENDREPLY, null);
		toButtons      = addressButtons("To", null, null);
		return requestButtons;
	}

	protected String constructTitle1() {
		String s = " " + Screen.node[me];
		return s;
	}

	protected String constructTitle2() {
		String s = " Send request to ";
		for (int i = 0; i < number; i++)
			if (needToSendRequest[i])
				s = s + Screen.node[i] + " ";
		if (wantToEnter && any(needToSendRequest))
			return s + "		";
		else
			return " ";
	}

	protected String constructRow1(int g) {
		String s = " " + Screen.node[g];
		if (replyReceived[g])
			s = s + " Reply received.";
		else if (!needToSendRequest[g] && wantToEnter)
			s = s + " Waiting for reply.";
		return s;
	}

	protected String constructRow2(int g) {
		String s = " ";
		if (needToSendReply[g])
			s = s + " Reply to " + Screen.node[g] + ".	 ";
		else if (deferred[g])
			s = s + " Defer reply to " + Screen.node[g] + ".	 ";
		return s;
	}

	protected boolean stateMachine(String b) {
		switch (state) {	// Note: all cases return, so no breaks!
			case enterState:
				if (b.equals(REQUEST)) {
					wantToEnter = true;
					for (int i = 0; i < number; i++)
						if (i != me)
							needToSendRequest[i]=true;
					changeState(requestreplyState, requestButtons, sendButtons);
					return true;
				} else if (b.equals(REPLY) && any(needToSendReply) && released) {
					whichPlan = replyCode;
					released = false;
					changeState(toState, requestButtons, toButtons);
					return true;
				} else
					return false;
			case requestreplyState:
				if (b.equals(SENDREQUEST) && any(needToSendRequest)) {
					whichPlan = requestCode;
					changeState(toState, sendButtons, toButtons);
					return true;
				} else if (b.equals(SENDREPLY) && any(needToSendReply) && released) {
					whichPlan = replyCode;
					released = false;
					changeState(toState, sendButtons, toButtons);
					return true;
				} else
					return false;
			case senddeferredState:
				if (b.equals(SENDREPLY) && released) {
					whichPlan = replyCode;
					released = false;
					changeState(todeferredState, sendButtons, toButtons);
					return true;
				}
				else
					return false;
			case toState:
			case todeferredState:
				toNode = FindNode(b);
				if (toNode == number)
					return false;
				else if (whichPlan == requestCode) {
					needToSendRequest[toNode] = false;
					changeState(requestreplyState, toButtons, sendButtons);
					send(requestCode, toNode, me, 0);
					return true;
				} else {	// whichPlan == replyCode
					if (needToSendReply[toNode])
						send(replyCode, toNode, me, 0);
					else
						return false;
					needToSendReply[toNode] = false;

					if ((state == todeferredState) && any(needToSendReply))
						changeState(senddeferredState, toButtons, sendButtons);
					else if (wantToEnter || any(deferred))
						changeState(requestreplyState, toButtons, sendButtons);
					else
						changeState(enterState, toButtons, requestButtons);
					return true;
				}
			case releaseState:
				if (b.equals(RELEASE)) {
					wantToEnter = false;
					for (int i = 0; i < number; i++) {
						if (i != me)
							send(releaseCode,i,me,0);
						released = true;
						if (deferred[i])
							needToSendReply[i] = true;
						deferred[i] = false;
						replyReceived[i] = false;
					}
					if (any(needToSendReply))
						changeState(senddeferredState, releaseButtons, sendButtons);
					else
						changeState(enterState, releaseButtons, requestButtons);
					return true;
				}
				else
					return false;
			default:
				return false;
		}
	}

	private boolean any(boolean[] a) {
		for (int i = 0; i < number; i++)
			if ((i != me) && a[i])
				return true;
		return false;
	}

	private boolean allRepliesReceived() {
		int count = 0;
		for (int i = 0; i < number; i++)
			if ((i != me) && (replyReceived[i])) count++;
		return count == number - 1;
	}

	protected void receive(int m, int by, int num) {
		switch (m) {
			case requestCode:
				if (!released)
					deferred[by] = true;
				else
					needToSendReply[by] = true;
				break;
			case replyCode:
				replyReceived[by] = true;
				if (allRepliesReceived()) {
					changeState(releaseState, sendButtons, releaseButtons);
					return;
				}
				break;
			case releaseCode:
				released = true;
				break;
		}
		redisplay();
	}

	// Data structure for messages

	private boolean wantToEnter = false;  // Want to enter CS
	private boolean released    = true;

	private boolean[] deferred          = new boolean[number];
	private boolean[] needToSendRequest = new boolean[number];
	private boolean[] needToSendReply   = new boolean[number];
	private boolean[] replyReceived     = new boolean[number];

	// State machine

	private int toNode;               // Node selected
	private int whichPlan;            // Action selected: request or reply
	private static final int requestCode  = 1;
	private static final int replyCode    = 2;
	private static final int releaseCode  = 3;

	private static final int enterState         = 0;
	private static final int releaseState       = 1;
	private static final int requestreplyState  = 2;
	private static final int toState            = 3;
	private static final int senddeferredState  = 4;
	private static final int todeferredState    = 5;

	// Components of user interface

	private int requestButtons, releaseButtons, sendButtons, toButtons;

	private static final String
		REQUEST      = "Inside CS",
		REPLY        = "Reply",
		RELEASE      = "Now!",
		SENDREQUEST  = "Request",
		SENDREPLY    = "Reply",
//		ALL          = "All",
		RELEASELABEL = " You can enter critical section.	Leave ";
}
