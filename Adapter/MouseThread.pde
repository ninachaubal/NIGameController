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
  
  
  void start () {
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
  void run () {
    
      while(running) {   
      //parallel runtime code starts here
      float mousedist =calcmousedist();
      float mousedistChange= mousedist-mousedistPrev;
      mousiex=mousedistChange;
      
      if(mousedistChange >0.18)
      {textMode(SCREEN);
           textSize(20);
           text("LEFT CLICK");
           if(!lcState){lcState=true; ricky.keyPress(KeyEvent.VK_L);}
         } else {if(lcState){lcState=false; ricky.keyRelease(KeyEvent.VK_L);}} 
      
      if(mousedistChange <-.17)
      {textMode(SCREEN);
           textSize(20);
           text("RIGHT CLICK");
           if(!rcState){rcState=true; ricky.keyPress(KeyEvent.VK_R);}
         } else {if(rcState){rcState=false; ricky.keyRelease(KeyEvent.VK_R);}} 
      
      mousedistPrev=mousedist;
      //mouse movement stuff
      if (mousedist>1.2) {enableMove=true;} else{enableMove=false; enableMovePrev=false;}
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
  void pause() {
    System.out.println("Quitting mouse thread."); 
    enableMove=false;

  }
  
  
  
  void checkEnableMove(){

  }
  

  

  

    
  
}
