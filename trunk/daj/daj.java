/*
      DAJ - Distributed Algorithms in Java.
	  Developed by Moti Ben-Ari and 
	  other programmers as listed in "About" frame.
      Copyright 2003-5 by Mordechai (Moti) Ben-Ari.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.
*/

//  Main method for application.
package daj;
class daj {
   public static void main(String[] args) {
     Screen.isApplication = true;
     String s0 = "", s1 = "";
     WaitObject waitObject = new WaitObject();
     if (args.length >= 1) s0 = args[0].toLowerCase();
     if (args.length >= 2) s1 = args[1];
     while (true) {
       new Screen(s0, s1, waitObject);
       waitObject.waitOK();
       s0 = ""; s1 = ""; // Arguments only for first execution 
     }
   }
}
