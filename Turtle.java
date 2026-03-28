public class Turtle {
    int age; 
    int energy; 
//    Image image; 
    
    public Turtle() {
        age = 0; 
        energy = 0;
        
    //    image = //image path; 
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    /*public void setImage(Image imgPath) {
        image = imgPath; 
    }*/
    
    public void addEnergy(int increase) {
        energy += increase;
    }
    
    public void removeEnergy(int decrease) {
        energy -= decrease; 
    }
    
    public int getAge() {
        return age;
    }
    
/*    public Image getImage() {
        return image;
    }*/
    
    public int getEnergy() {
        return energy; 
    }
}
