package Coffee;

class Cappuccino implements Coffee {
    private CoffeeSize size;
    Cappuccino (CoffeeSize size) {this.size = size;}
    public String getDescription(){return "Espresso (" + size + ")";}
    public double getCost() {return 3.0 + size.getMultiplier();}
}
