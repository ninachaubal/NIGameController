import processing.core.*; 
import processing.xml.*; 

import SimpleOpenNI.*; 
import java.awt.Robot; 
import java.awt.AWTException; 
import java.awt.event.InputEvent; 
import javax.swing.JFileChooser; 
import java.io.BufferedReader; 
import java.io.File; 
import java.io.FileNotFoundException; 
import java.io.FileReader; 
import java.io.IOException; 
import java.lang.reflect.Constructor; 
import java.lang.reflect.Field; 
import java.awt.MouseInfo; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Adapter extends PApplet {

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
//context is the kinect object

SimpleOpenNI  context;

 //simulate keypresses













Robot ricky; //creates object "ricky" of robot class
//user control: track most recent user because only one user shall have control
int currentuser = 0;
Skeleton s = new Skeleton(0);

File file;
ArrayList<Gesture> gestures;
HashMap<String, Integer> keyMap;
boolean mouseEnabled = false;
float mouseSensitivity = 1f;

public void setup() {
    setupKeyMap();
    size(screen.height*4/3, screen.height, P3D); //Keep 4/3 aspect ratio, since it matches the kinect's.
    //oscP5 = new OscP5(this, "127.0.0.1", 7110);
    
    context = new SimpleOpenNI(this);
    // enable depthMap generation 
    context.enableDepth();
    // enable skeleton generation for all joints
    context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
    //context.enableRGB();
    try //standard Robot class error check
    {
        ricky = new Robot();
    }
        catch (AWTException e)
    {
        println("Robot class not supported by your system!");
        exit();
    }
    
    openFile();
    parseFile();
    
    if(mouseEnabled){
        new MouseThread().start();
    }
}

public void setupKeyMap(){
  keyMap = new HashMap<String, Integer>();
  Field[] fields = KeyEvent.class.getDeclaredFields();
  for (Field field : fields) {
    String name = field.getName();
    if (name.startsWith("VK_")) {
        try{
        keyMap.put(name.substring("VK_".length()).toLowerCase(),
                          field.getInt(null));
       }
       catch(Exception e){
         e.printStackTrace();
       }
    }
  }
}

public void openFile() {
  JFileChooser fc = new JFileChooser();
  int ret = fc.showOpenDialog(this);
  if (ret == JFileChooser.APPROVE_OPTION) {
    file = fc.getSelectedFile();
  }
}

public void parseFile(){
  gestures = new ArrayList<Gesture>();
  if(file != null){
  
  try{
    BufferedReader in = new BufferedReader(new FileReader(file));
      String line;
      while ((line = in.readLine()) != null) {
        String vals[] = line.split(",");
         for(int i=0;i<vals.length;i++){
          vals[i] = vals[i].trim();
        }
        if(vals[0].equalsIgnoreCase("Gesture")) continue; //skip heading line in csv
        if(vals[0].equalsIgnoreCase("mouse") && 
           (vals[1].equalsIgnoreCase("") || vals[1].equalsIgnoreCase("enabled") || vals[1].equalsIgnoreCase("on"))) {
           mouseEnabled = true;
           try {
            mouseSensitivity = Float.parseFloat(vals[2]);
            if (mouseSensitivity < 0.3f) mouseSensitivity = 1f;
          }
          catch (NumberFormatException e){
            mouseSensitivity = 1f;
          } 
          continue;
        }
        String keys[] = vals[1].split("\\+");
        for(int i=0;i<keys.length;i++){
          keys[i] = keys[i].trim().toLowerCase();
        }
        float sensitivity = 1f;
        if (vals.length > 2){
          try {
            sensitivity = Float.parseFloat(vals[2]);
            if (sensitivity < 0.3f) sensitivity = 1f;
          }
          catch (NumberFormatException e){
            sensitivity = 1f;
          }
        }
        boolean hold = true;
        if (vals.length > 3){
          if(vals[3].equalsIgnoreCase("hit"))
            hold = false;
        }
        try {
          Class clas = Class.forName("Adapter$"+vals[0]);
          Constructor cons = clas.getConstructor(new Class[] {Adapter.class});
          Gesture g = (Gesture) cons.newInstance(this);
          g.keys = keys;
          g.hold = hold;
          if (vals.length > 4){
            g.description = vals[4];
          }
          g.sensitivity = sensitivity;
          gestures.add(g);
        } 
        catch (Exception e) {
          continue;
        }
      }
      in.close();
  }
  catch (FileNotFoundException e){
    e.printStackTrace();
  }
  catch (IOException e){
    e.printStackTrace();
    }
  }
}
//Update skeleton
public void updateSkeleton(){
  //msg.print();
  PVector skel_head = new PVector();
  s.headConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_HEAD,skel_head);
      s.headCoords[0] = -skel_head.x;
      s.headCoords[1] = -skel_head.y;
      s.headCoords[2] = skel_head.z;
  PVector skel_neck = new PVector();    
  s.neckConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_NECK,skel_neck);
      s.neckCoords[0] = -skel_neck.x;
      s.neckCoords[1] = -skel_neck.y;
      s.neckCoords[2] = skel_neck.z;    
  PVector skel_rShoulder = new PVector();    
  s.rShoulderConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_SHOULDER,skel_rShoulder);
      s.rShoulderCoords[0] = -skel_rShoulder.x;
      s.rShoulderCoords[1] = -skel_rShoulder.y;
      s.rShoulderCoords[2] = skel_rShoulder.z;  
  PVector skel_lShoulder = new PVector();    
  s.lShoulderConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_SHOULDER,skel_lShoulder);
      s.lShoulderCoords[0] = -skel_lShoulder.x;
      s.lShoulderCoords[1] = -skel_lShoulder.y;
      s.lShoulderCoords[2] = skel_lShoulder.z;  
  PVector skel_rElbow = new PVector();    
  s.rElbowConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_ELBOW,skel_rElbow);
      s.rElbowCoords[0] = -skel_rElbow.x;
      s.rElbowCoords[1] = -skel_rElbow.y;
      s.rElbowCoords[2] = skel_rElbow.z;  
  PVector skel_lElbow = new PVector();    
  s.lElbowConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_ELBOW,skel_lElbow);
      s.lElbowCoords[0] = -skel_lElbow.x;
      s.lElbowCoords[1] = -skel_lElbow.y;
      s.lElbowCoords[2] = skel_lElbow.z; 
  PVector skel_rHand = new PVector();    
  s.rHandConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_HAND,skel_rHand);
      s.rHandCoords[0] = -skel_rHand.x;
      s.rHandCoords[1] = -skel_rHand.y;
      s.rHandCoords[2] = skel_rHand.z;    
  PVector skel_lHand = new PVector();    
  s.lHandConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_HAND,skel_lHand);
      s.lHandCoords[0] = -skel_lHand.x;
      s.lHandCoords[1] = -skel_lHand.y;
      s.lHandCoords[2] = skel_lHand.z;  
  PVector skel_torso = new PVector();    
  s.torsoConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_TORSO,skel_torso);
      s.torsoCoords[0] = -skel_torso.x;
      s.torsoCoords[1] = -skel_torso.y;
      s.torsoCoords[2] = skel_torso.z;     
  PVector skel_rHip = new PVector();    
  s.rHipConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_HIP,skel_rHip);
      s.rHipCoords[0] = -skel_rHip.x;
      s.rHipCoords[1] = -skel_rHip.y;
      s.rHipCoords[2] = skel_rHip.z;    
  PVector skel_lHip = new PVector();    
  s.lHipConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_HIP,skel_lHip);
      s.lHipCoords[0] = -skel_lHip.x;
      s.lHipCoords[1] = -skel_lHip.y;
      s.lHipCoords[2] = skel_lHip.z;     
  PVector skel_rKnee = new PVector();    
  s.rKneeConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_KNEE,skel_rKnee);
      s.rKneeCoords[0] = -skel_rKnee.x;
      s.rKneeCoords[1] = -skel_rKnee.y;
      s.rKneeCoords[2] = skel_rKnee.z;    
  PVector skel_lKnee = new PVector();    
  s.lKneeConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_KNEE,skel_lKnee);
      s.lKneeCoords[0] = -skel_lKnee.x;
      s.lKneeCoords[1] = -skel_lKnee.y;
      s.lKneeCoords[2] = skel_lKnee.z;
  PVector skel_rFoot = new PVector();    
  s.rFootConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_RIGHT_FOOT,skel_rFoot);
      s.rFootCoords[0] = -skel_rFoot.x;
      s.rFootCoords[1] = -skel_rFoot.y;
      s.rFootCoords[2] = skel_rFoot.z;    
  PVector skel_lFoot = new PVector();    
  s.lFootConf = (int)context.getJointPositionSkeleton(currentuser,SimpleOpenNI.SKEL_LEFT_FOOT,skel_lFoot);
      s.lFootCoords[0] = -skel_lFoot.x;
      s.lFootCoords[1] = -skel_lFoot.y;
      s.lFootCoords[2] = skel_lFoot.z;
}

//----------SimpleOpenNI function------------
public void onNewUser(int userId)
{
  println("New user with ID = " + userId);
  println("Start pose detection");
  context.startPoseDetection("Psi",userId);
}

public void onLostUser(int userId)
{
  println("Lost User " + userId);
  if(userId==currentuser){
  currentuser=0; s.id = 0;}
}

public void onStartCalibration(int userId)
{
  println("Caliberating User " + userId);
}

public void onEndCalibration(int userId, boolean successfull)
{
  //println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
  if (successfull) 
  { 
    java.awt.Toolkit.getDefaultToolkit().beep();
    println("User "+userId+" Calibrated successfully!!!");
    //New skeleton calibrated! Lets create it!
    currentuser = userId;
    s.id = currentuser;
    //And then start tracking it!
    context.startTrackingSkeleton(userId); 
  } 
  else 
  { 
    println("Failed to calibrate user "+userId+"!!!");
    println("Start pose detection");
    context.startPoseDetection("Psi",userId);
  }
}

public void onStartPose(String pose,int userId)
{
  //println("onStartPose - userId: " + userId + ", pose: " + pose);
  print("Stop pose detection,");
  context.stopPoseDetection(userId); 
  context.requestCalibrationSkeleton(userId, true);
}
//----------SimpleOpenNI function end here ------------



public void drawline(float[] pointa, float[] pointb, int r, int g, int b, int weight){
//draws line between two float coordinates (3d arrays)  
    stroke(r,g,b);
    strokeWeight(weight); 
    line(pointa[0], pointa[1], -pointa[2], pointb[0], pointb[1], -pointb[2]);
}

public float calcangle(float[] left, float[] middle, float[] right){
//calculate angle between three points: side1, middle, side 2
//if first two arguments are same, measure v2's angle with vertical axis
//if last two arguments are the same, measure v1's angle with horizontal plane
    float v1[] = new float[3];
    v1[0] = left[0]-middle[0];
    if((left[0]==middle[0])&&(left[1]==middle[1])&&(left[2]==middle[2])) {v1[1]=1;} else {v1[1] = left[1]-middle[1];}
    v1[2] = left[2]-middle[2];

    float v2[] = new float[3];
    if((right[0]==middle[0])&&(right[1]==middle[1])&&(right[2]==middle[2])) {v2[0]=left[0]-middle[0];} else {v2[0] = right[0]-middle[0];}
    v2[1] = right[1]-middle[1];
    if((right[0]==middle[0])&&(right[1]==middle[1])&&(right[2]==middle[2])) {v2[2]=left[2]-middle[2];} else {v2[2] = right[2]-middle[2];}

    float vmag1 = sqrt(v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]);
    float vmag2 = sqrt(v2[0]*v2[0]+v2[1]*v2[1]+v2[2]*v2[2]);
    float dotprod = v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    float angle = acos(dotprod/(vmag1*vmag2));
    return angle;
}

public float calcanglexz(float[] a, float[] b){
//calculate angle between x axis and projection of vector onto x-z plane
    float v[] = new float[2];
    v[0] = a[0]-b[0];
    v[1] = a[2]-b[2]; //since x-Z plane
    float angle = atan(v[1]/v[0]);
    return angle;
}

public float calcanglelean(float[] a, float[] b){
//calculate angle between x axis and projection of vector onto x-z plane
    float v2[] = new float[2];
    v2[0] = a[0]-b[0];
    v2[1] = a[2]-b[2];

    float vmag1 = a[1]-b[1];
    float vmag2 = sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
    float angle = atan(vmag2/vmag1);

    if(v2[1]>0){return angle;} else {return -angle;}
}

public float calcanglesteer(float[] a, float[] b){
//calculate angle between x axis and projection of vector onto x-z plane
    float v2[] = new float[2];
    v2[0] = a[0]-b[0];
    v2[1] = a[2]-b[2];

    float vmag1 = a[1]-b[1];
    float vmag2 = sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
    float angle = atan(vmag2/vmag1);

return angle;
   // if(a[1]>b[1]){return angle;} else {return -angle;}
}


public float calcangleflat(float[] pointa1, float[] pointa2, float[] pointa3){
  //calculate the angle between two vector's projection onto x-z plane
   float v1[] = new float[3];
   float v2[] = new float[3];
   
   float vmid[] = {0,0,0};
  v1[0]=pointa1[0]-pointa2[0];
  v1[1]=0;
  v1[2]=pointa1[2]-pointa2[2];
  v2[0]=pointa2[0]-pointa3[0];
  v2[1]=0;
  v2[2]=pointa2[2]-pointa3[2];
  
  return calcangle(v1, vmid, v2);
}

public float calcdistance(float[] pointa, float[] pointb){
//calculates the distance between two points  
    float v[] = new float[3];
    v[0] = pointa[0]-pointb[0];
    v[1] = pointa[1]-pointb[1];
    v[2] = pointa[2]-pointb[2];  
    return sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
}

public float calcmiddistance(float[] pointa1, float[] pointa2, float[] pointb1, float[] pointb2){
//calculates the distance between midpoints of two vectors a, b
//format: a1, a2, b1, b2
    float v1[] = new float[3];
    v1[0] = (pointa1[0]+pointa2[0])/2;
    v1[1] = (pointa1[1]+pointa2[1])/2;
    v1[2] = (pointa1[2]+pointa2[2])/2;  
    
    float v2[] = new float[3];
    v2[0] = (pointb1[0]+pointb2[0])/2;
    v2[1] = (pointb1[1]+pointb2[1])/2;
    v2[2] = (pointb1[2]+pointb2[2])/2;
    
    return calcdistance(v1, v2);    
}

public float calcmousedist(){
//calculates the hand stretch to front (for mouse activate) scaled to body size 
    float shoulderLength = calcdistance(s.lShoulderCoords, s.rShoulderCoords);
    float v1[] = new float[3];
    v1[0] = s.lShoulderCoords[0]-s.lHandCoords[0];
    v1[1] = 0;
    v1[2] = s.lShoulderCoords[2]-s.lHandCoords[2];  
    float v2[] = {0,0,0};
    return (calcdistance(v1, v2)/shoulderLength);
}

public float calcmovedist(){
//calculates the hand stretch to front (for mouse activate) scaled to body size 
    
    float v1[] = new float[2];
    float v2[] = new float[2];
    float v1mag, v2mag;
    v1[0] = s.rShoulderCoords[0]-s.rHandCoords[0];
    v1[1] = s.rShoulderCoords[2]-s.rHandCoords[2];  
    v1mag = sqrt(v1[0]*v1[0]+v1[1]*v1[1]);
    
    v2[0] = s.rShoulderCoords[0]-s.lShoulderCoords[0];
    v2[1] = s.rShoulderCoords[2]-s.lShoulderCoords[2]; 
    v2mag = sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
    
    float crossprod = v1[0]*v2[1]-v1[1]*v2[0];
    return asin(crossprod/(v1mag*v2mag))*v1mag ;
}

public void check(){
  for (Gesture g : gestures){
    boolean eval = g.eval();
    if(eval){ 
      textMode(SCREEN);
      textSize(50);
      text(g.description, 0, 50);
    }
    
    if (g.state ^ eval){
      handle(g);
    }
  }
}

public void handle(Gesture g){
  
  if(!g.state){
  	g.state=true;
  	for (int i=0;i<g.keys.length;i++){
            Integer keycode = (Integer)keyMap.get(g.keys[i]);
            if (keycode != null)
              ricky.keyPress(keycode.intValue());
  	}
        if(!g.hold) {
          for (int i=g.keys.length-1;i>=0;i--){
             Integer keycode = (Integer)keyMap.get(g.keys[i]); 
             if (keycode != null)
               ricky.keyRelease(keycode.intValue());
          }
        }
  }
  else {
  	g.state=false; 
        if(g.hold){
  	  for (int i=g.keys.length-1;i>=0;i--){
             Integer keycode = (Integer)keyMap.get(g.keys[i]);
             if (keycode != null)
               ricky.keyRelease(keycode.intValue());
  	  }
        }
  }
}

public void draw()
{
  
  //println(MouseInfo.getPointerInfo().getLocation().x);
  background(0);  

  ambientLight(64, 64, 64);
  lightSpecular(255,255,255);
  directionalLight(224,224,224, .5f, 1, -1);

  // update the cam
  context.update();
  //update skeleton coordinates
  updateSkeleton();
  
  
  //draw all the spheres at joints
  if(currentuser!=0){  
    noStroke();
    fill(255, 255, 255);
    for (float[] j: s.allCoords) {
      pushMatrix();
      translate(j[0], j[1], -j[2]);
      sphere(40);
      popMatrix();
    }          
  }
    
  //check for gestures
  check();
  
   //for debugging
   //text(calcanglesteer(s.rHandCoords, s.lHandCoords), 0, 40); //elbow angle

}//end of animation thread



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
  boolean state;
  boolean hold;
  float sensitivity;
  String description;
  String keys[];
  public abstract boolean eval();
}

class HandStretchForward extends Gesture {
  public HandStretchForward(){
    state = false;
    description = "Hand Stretch Forward";
  }
  public boolean eval(){
    return
    (calcangle(s.rHandCoords, s.rElbowCoords, s.rShoulderCoords)>2.3f) && (calcangle(s.lHandCoords, s.lElbowCoords, s.lShoulderCoords)>2.3f) &&
    (calcangle(s.rHandCoords, s.rElbowCoords, s.rHipCoords)>1.8f) && (calcangle(s.lHandCoords, s.lElbowCoords, s.lHipCoords)>1.8f);
  }
}

class HandStretchOutward extends Gesture {
  public HandStretchOutward(){
    state = false;
    description = "Hand Stretch Outward";
  }
  public boolean eval(){
    return (calcdistance(s.lHandCoords, s.rHandCoords)/calcdistance(s.lShoulderCoords, s.rShoulderCoords))>4.5f;
  }
}

class SteerLeft extends Gesture {
  public SteerLeft(){
    state = false;
    description = "Steer Left";
  }
  public boolean eval(){
    return (calcanglesteer(s.rHandCoords, s.lHandCoords)>0)&&(calcanglesteer(s.rHandCoords, s.lHandCoords)<0.9f);
  }
}

class SteerRight extends Gesture {
  public SteerRight(){
    state = false;
    description = "Steer Right";
  }
  public boolean eval(){
     return (calcanglesteer(s.rHandCoords, s.lHandCoords)<0)&&(calcanglesteer(s.rHandCoords, s.lHandCoords)>-0.9f);
  }
}

class LeanForward extends Gesture {
  public LeanForward(){
    state = false;
    description = "Lean Forward";
  }
  public boolean eval(){
     return (calcanglelean(s.torsoCoords, s.neckCoords)>0.4f)&&
          ( (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<-1.35f)||(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>1.35f));
  }
}   
    
class LeanBackward extends Gesture {
  public LeanBackward(){
    state = false;
    description = "Lean Backward";
  }
  public boolean eval(){
     return (calcanglelean(s.torsoCoords, s.neckCoords)<0)&&
          ( (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<-1.35f)||(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>1.35f));
  }
}   

class LeanLeft extends Gesture {
  public LeanLeft(){
    state = false;
    description = "Lean Left";
  }
  public boolean eval(){
     return (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>0)&&(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<1.35f);
  }
}   
    
class LeanRight extends Gesture {
  public LeanRight(){
    state = false;
    description = "Lean Right";
  }
  public boolean eval(){
     return (calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)<0)&&(calcanglesteer(s.rShoulderCoords, s.lShoulderCoords)>-1.35f);
  }
}   

class TwistLeft extends Gesture {
  public TwistLeft(){
    state = false;
    description = "Twist Left";
  }
  public boolean eval(){
     return (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)<(-0.5f)) &&
            (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)> (-1) );
  }
}   
    
class TwistRight extends Gesture {
  public TwistRight(){
    state = false;
    description = "Twist Right";
  }
  public boolean eval(){
     return (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)>0.5f) &&
            (calcanglexz(s.lShoulderCoords, s.rShoulderCoords)<1 );
  }
}

class SelfKnucklePunch extends Gesture {
  public SelfKnucklePunch(){
    state = false;
    description = "/_ _\\";
  }
  public boolean eval(){
     return (calcangle(s.lElbowCoords, s.lHandCoords, s.rElbowCoords)>2.7f);
  }
}

class ForearmForward extends Gesture {
  public ForearmForward(){
    state = false;
    description = "Forearm Forward";
  }
  public boolean eval(){
     return (calcanglelean(s.rShoulderCoords, s.rHandCoords)<-0.80f)&&(s.rHandCoords[1]<s.rHipCoords[1]);
  }
}

class ForearmBackward extends Gesture {
  public ForearmBackward(){
    state = false;
    description = "Forearm Backward";
  }
  public boolean eval(){
     return (calcanglelean(s.rShoulderCoords, s.rHandCoords)<0.2f)&&(calcanglelean(s.rShoulderCoords, s.rHandCoords)>-0.4f)&&(s.rHandCoords[1]<s.rHipCoords[1]);
  }
}

class LeftHandSwingForward extends Gesture {
  public LeftHandSwingForward(){
    state = false;
    description = "Left Hand Swing Forward";
  }
  public boolean eval(){
     return (calcmovedist()>320);
  }
}

class LeftHandSwingBackward extends Gesture {
  public LeftHandSwingBackward(){
    state = false;
    description = "Left Hand Swing Backward";
  }
  public boolean eval(){
     return (calcmovedist()<-230);
  }
}
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
class MouseThread extends Thread {
  

  boolean running;
  boolean enableMove;
  boolean enableMovePrev;
  boolean lcState, rcState;
  
  int stage = 0;  //variable for which stage in gesture sequence 
  
  float mousePrevX;
  float mousePrevY;
  
  float mousedistPrev;
  
  float mousiex, mousiey;
  
  int statusarc =0;  //status bar variable
    
  MouseThread() {
    stage =0;
    running = false;
    enableMove = false; enableMovePrev=false;
  }
  
  
  public void start () {
    // Set running equal to true
       running = true;
    //enable keep false   
       enableMove = false;
       enableMovePrev = false;
    // Print messages
    println("Starting mouse control thread"); 
    // Do whatever start does in Thread, don't forget this!
    super.start();
  }
 
 
  // Runtime method, this gets triggered by start()
  public void run () {
    
      while(running) {   
      //parallel runtime code starts here
      float mousedist =calcmousedist();
      float mousedistChange= mousedist-mousedistPrev;
      mousiex=mousedistChange;
      
      if(mousedistChange >0.18f)
      {textMode(SCREEN);
           textSize(50);
           text("LEFT CLICK", 0, 50);
           if(!lcState){lcState=true; ricky.keyPress(KeyEvent.VK_L);}
         } else {if(lcState){lcState=false; ricky.keyRelease(KeyEvent.VK_L);}} 
      
      if(mousedistChange <-.17f)
      {textMode(SCREEN);
           textSize(50);
           text("RIGHT CLICK", 0, 50);
           if(!rcState){rcState=true; ricky.keyPress(KeyEvent.VK_R);}
         } else {if(rcState){rcState=false; ricky.keyRelease(KeyEvent.VK_R);}} 
      
      mousedistPrev=mousedist;
      //mouse movement stuff
      if (mousedist>1.2f) {enableMove=true;} else{enableMove=false; enableMovePrev=false;}
      if (enableMove){  //enable acts like whether the thread is on or off
      
        //neck is a reference point, shoulderlength scales to body size
        float shoulderLength = calcdistance(s.lShoulderCoords, s.rShoulderCoords);
        float mouseCurrX=(s.lHandCoords[0]-s.neckCoords[0])/shoulderLength;       
        float mouseCurrY=(s.lHandCoords[1]-s.neckCoords[1])/shoulderLength;   
        
        if(!enableMovePrev){
          mousePrevX=mouseCurrX;
          mousePrevY=mouseCurrY;}
        
        
        float mouseChangeX=mouseCurrX-mousePrevX; //speed
        float mouseChangeY=mouseCurrY-mousePrevY; //speed
        

        
        ricky.mouseMove(MouseInfo.getPointerInfo().getLocation().x+(int)(700*mouseChangeX),MouseInfo.getPointerInfo().getLocation().y+(int)(700*mouseChangeY));
        
        //mousiex=mouseChangeX;
        //mousiey=mouseChangeY;
        
          mousePrevX=mouseCurrX;
          mousePrevY=mouseCurrY;
         
              
              enableMovePrev=true;
      }
      
      
      
      
      
      
      //parallel runtime code ends here
            try {
            sleep((long)(10)); //teensy bit of delay (10ms) to have a sense of time
            } catch (Exception e) {
            }
      } //while running thingy

   
}
  
  
  // Method that quits the thread
  public void pause() {
    System.out.println("Quitting mouse thread."); 
    enableMove=false;

  }
  
  
  
  public void checkEnableMove(){

  }
  

  

  

    
  
}
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
class Skeleton {
  // We just use this class as a structure to store the joint coordinates sent by OSC.
  // The format is {x, y, z}, where x and y are in the [0.0, 1.0] interval, 
  // and z is in the [0.0, 7.0] interval.
  float headCoords[] = new float[3];
  float neckCoords[] = new float[3];
  float rShoulderCoords[] = new float[3];
  float rElbowCoords[] = new float[3];
  float rHandCoords[] = new float[3];
  float lShoulderCoords[] = new float[3];
  float lElbowCoords[] = new float[3];    
  float lHandCoords[] = new float[3];   
  float torsoCoords[] = new float[3];
  float rHipCoords[] = new float[3];
  float rKneeCoords[] = new float[3];   
  float rFootCoords[] = new float[3];
  float lHipCoords[] = new float[3];
  float lKneeCoords[] = new float[3];  
  float lFootCoords[] = new float[3];
  float[] allCoords[] = {headCoords, neckCoords, rShoulderCoords, rElbowCoords,
                       rHandCoords, lShoulderCoords, lElbowCoords,
                       lHandCoords, torsoCoords, rHipCoords, /*rKneeCoords,
                       rFootCoords,*/ lHipCoords, /*lKneeCoords, lFootCoords*/};
                       
  int headConf;
  int neckConf;
  int rShoulderConf;
  int rElbowConf;
  int rHandConf;
  int lShoulderConf;
  int lElbowConf;    
  int lHandConf;   
  int torsoConf;
  int rHipConf;
  int rKneeConf;   
  int rFootConf;
  int lHipConf;
  int lKneeConf;  
  int lFootConf;
                      
  int id; //here we store the skeleton's ID as assigned by OpenNI and sent through OSC.
  float colors[] = {255, 255, 255};// The color of this skeleton

  Skeleton(int id) {
    this.id = id;
    colors[0] = random(128, 255);
    colors[1] = random(128, 255);
    colors[2] = random(128, 255);
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "Adapter" });
  }
}
