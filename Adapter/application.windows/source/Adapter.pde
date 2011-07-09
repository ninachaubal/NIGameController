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
import SimpleOpenNI.*;
SimpleOpenNI  context;

import java.awt.Robot; //simulate keypresses
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

Robot ricky; //creates object "ricky" of robot class
//user control: track most recent user because only one user shall have control
int currentuser = 0;
Skeleton s = new Skeleton(0);

File file;
ArrayList<Gesture> gestures;
HashMap<String, Integer> keyMap;
boolean mouseEnabled = false;
float mouseSensitivity = 1f;

void setup() {
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

void setupKeyMap(){
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

void openFile() {
  JFileChooser fc = new JFileChooser();
  int ret = fc.showOpenDialog(this);
  if (ret == JFileChooser.APPROVE_OPTION) {
    file = fc.getSelectedFile();
  }
}

void parseFile(){
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
            if (mouseSensitivity < 0.3) mouseSensitivity = 1f;
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
            if (sensitivity < 0.3) sensitivity = 1f;
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
void updateSkeleton(){
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
void onNewUser(int userId)
{
  println("New user with ID = " + userId);
  println("Start pose detection");
  context.startPoseDetection("Psi",userId);
}

void onLostUser(int userId)
{
  println("Lost User " + userId);
  if(userId==currentuser){
  currentuser=0; s.id = 0;}
}

void onStartCalibration(int userId)
{
  println("Caliberating User " + userId);
}

void onEndCalibration(int userId, boolean successfull)
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

void onStartPose(String pose,int userId)
{
  //println("onStartPose - userId: " + userId + ", pose: " + pose);
  print("Stop pose detection,");
  context.stopPoseDetection(userId); 
  context.requestCalibrationSkeleton(userId, true);
}
//----------SimpleOpenNI function end here ------------



void drawline(float[] pointa, float[] pointb, int r, int g, int b, int weight){
//draws line between two float coordinates (3d arrays)  
    stroke(r,g,b);
    strokeWeight(weight); 
    line(pointa[0], pointa[1], -pointa[2], pointb[0], pointb[1], -pointb[2]);
}

float calcangle(float[] left, float[] middle, float[] right){
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

float calcanglexz(float[] a, float[] b){
//calculate angle between x axis and projection of vector onto x-z plane
    float v[] = new float[2];
    v[0] = a[0]-b[0];
    v[1] = a[2]-b[2]; //since x-Z plane
    float angle = atan(v[1]/v[0]);
    return angle;
}

float calcanglelean(float[] a, float[] b){
//calculate angle between x axis and projection of vector onto x-z plane
    float v2[] = new float[2];
    v2[0] = a[0]-b[0];
    v2[1] = a[2]-b[2];

    float vmag1 = a[1]-b[1];
    float vmag2 = sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
    float angle = atan(vmag2/vmag1);

    if(v2[1]>0){return angle;} else {return -angle;}
}

float calcanglesteer(float[] a, float[] b){
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


float calcangleflat(float[] pointa1, float[] pointa2, float[] pointa3){
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

float calcdistance(float[] pointa, float[] pointb){
//calculates the distance between two points  
    float v[] = new float[3];
    v[0] = pointa[0]-pointb[0];
    v[1] = pointa[1]-pointb[1];
    v[2] = pointa[2]-pointb[2];  
    return sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
}

float calcmiddistance(float[] pointa1, float[] pointa2, float[] pointb1, float[] pointb2){
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

float calcmousedist(){
//calculates the hand stretch to front (for mouse activate) scaled to body size 
    float shoulderLength = calcdistance(s.lShoulderCoords, s.rShoulderCoords);
    float v1[] = new float[3];
    v1[0] = s.lShoulderCoords[0]-s.lHandCoords[0];
    v1[1] = 0;
    v1[2] = s.lShoulderCoords[2]-s.lHandCoords[2];  
    float v2[] = {0,0,0};
    return (calcdistance(v1, v2)/shoulderLength);
}

float calcmovedist(){
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

void check(){
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

void handle(Gesture g){
  
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

void draw()
{
  
  //println(MouseInfo.getPointerInfo().getLocation().x);
  background(0);  

  ambientLight(64, 64, 64);
  lightSpecular(255,255,255);
  directionalLight(224,224,224, .5, 1, -1);

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



