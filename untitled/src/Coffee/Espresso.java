package Coffee;

class Espresso implements Coffee {
    private CoffeeSize size;
    Espresso(CoffeeSize size) {this.size = size;}
    public String getDescription(){return "Espresso (" + size + ")";}
    public double getCost() {return 2.0 + size.getMultiplier();}
}
