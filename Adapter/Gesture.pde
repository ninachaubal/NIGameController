/*
    This file is part of NIGameController.
    Copyright (C) 2011 Rajarshi Roy and Niraj Chaubal.
    
    NIGameController is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NIGameController is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NIGameController.  If not, see <http://www.gnu.org/licenses/>.
*/
abstract class Gesture {
  /*This is the parent Gesture class. New gestures can be defined by 
    creating subclasses of Gesture and overloading the eval() function
    
    the hold, sensitivity, description and keys variables are set on the
    basis of the configuration file supplied by the user.
    Since the description is optional in the configuration file, it is 
    recommended to set a default value for description in the subclass
    constructor.   
   */
  boolean state; //keeps track of whether the gesture is active
  boolean hold;  //should the keys be held in a pressed down state if the gesture is active
  float sensitivity; // can be used as multiplying factor to affect gestures
  String description; // description displayed in the application's screen
  String keys[]; // array of keys to press
  abstract boolean eval(); //function to determine whether or not the gesture is active
}

class HandStretchForward extends Gesture {
  public HandStretchForward(){
    state = false;
    description = "Hand Stretch Forward";
  }
  boolean eval(){
    return
    (calcangle(s.rHandCoords, s.rElbowCoords, s.rShoulderCoords)>2.3) && (calcangle(s.lHandCoords, s.lElbowCoords, s.lShoulderCoords)>2.3) &&
    (calcangle(s.rHandCoords, s.rElbowCoords, s.rHipCoords)>1.8) && (calcangle(s.lHandCoords, s.lElbowCoords, s.lHipCoords)>1.8);
  }
}

class HandStretchOutward extends Gesture {
  public HandStretchOutward(){
    state = false;
    description = "Hand Stretch Outward";
  }
  boolean eval(){
    return (calcdistance(s.lHandCoords, s.rHandCoords)/calcdistance(s.lShoulderCoords, s.rShoulderCoords))>4.5;
  }
}

class SteerLeft extends Gesture {
  public SteerLeft(){
    state = false;
    description = "Steer Left";
  }
  boolean eval(){
    return (calcanglesteer(s.rHandCoords, s.lHandCoords)>0)&&(calcanglesteer(s.rHandCoords, s.lHandCoords)<0.9);
  }
}

class SteerRight extends Gesture {
  public SteerRight(){
    state = false;
    description = "Steer Right";
  }
  boolean eval(){
     return (calcanglesteer(s.rHandCoords, s.lHandCoords)<0)&&(calcanglesteer(s.rHandCoords, s.lHandCoords)>-0.9);
  }
}

class LeanForward extends Gesture {
  public LeanForward(){
    state = false;
    description = "Lean Forward";
  }
  boolean eval(){
     return (calcanglelean(s.torsoCoords, s.neckCoords)>0.4)&&
          ( (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<-1.35)||(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>1.35));
  }
}   
    
class LeanBackward extends Gesture {
  public LeanBackward(){
    state = false;
    description = "Lean Backward";
  }
  boolean eval(){
     return (calcanglelean(s.torsoCoords, s.neckCoords)<0)&&
          ( (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<-1.35)||(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>1.35));
  }
}   

class LeanLeft extends Gesture {
  public LeanLeft(){
    state = false;
    description = "Lean Left";
  }
  boolean eval(){
     return (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>0)&&(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<1.35);
  }
}   
    
class LeanRight extends Gesture {
  public LeanRight(){
    state = false;
    description = "Lean Right";
  }
  boolean eval(){
     return (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<0)&&(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>-1.35);
  }
}   

class TwistLeft extends Gesture {
  public TwistLeft(){
    state = false;
    description = "Twist Left";
  }
  boolean eval(){
     return (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)<(-0.5)) &&
            (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)> (-1) );
  }
}   
    
class TwistRight extends Gesture {
  public TwistRight(){
    state = false;
    description = "Twist Right";
  }
  boolean eval(){
     return (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)>0.5) &&
            (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)<1 );
  }
}

class SelfKnucklePunch extends Gesture {
  public SelfKnucklePunch(){
    state = false;
    description = "/_ _\\";
  }
  boolean eval(){
     return (calcangle(s.lElbowCoords, s.lHandCoords, s.rElbowCoords)>2.7);
  }
}

class ForearmForward extends Gesture {
  public ForearmForward(){
    state = false;
    description = "Forearm Forward";
  }
  boolean eval(){
     return (calcanglelean(s.rShoulderCoords, s.rHandCoords)<-0.80)&&(s.rHandCoords[1]<s.rHipCoords[1]);
  }
}

class ForearmBackward extends Gesture {
  public ForearmBackward(){
    state = false;
    description = "Forearm Backward";
  }
  boolean eval(){
     return (calcanglelean(s.rShoulderCoords, s.rHandCoords)<0.2)&&(calcanglelean(s.rShoulderCoords, s.rHandCoords)>-0.4)&&(s.rHandCoords[1]<s.rHipCoords[1]);
  }
}

class LeftHandSwingForward extends Gesture {
  public LeftHandSwingForward(){
    state = false;
    description = "Left Hand Swing Forward";
  }
  boolean eval(){
     return (calcmovedist()>320);
  }
}

class LeftHandSwingBackward extends Gesture {
  public LeftHandSwingBackward(){
    state = false;
    description = "Left Hand Swing Backward";
  }
  boolean eval(){
     return (calcmovedist()<-230);
  }
}
