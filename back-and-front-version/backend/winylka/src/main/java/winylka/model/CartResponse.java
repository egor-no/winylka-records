package winylka.model;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {

    public static class Line {
        private Product item;
        private int amount;
        private int lineTotal;

        public Product getItem() { return item; }
        public void setItem(Product item) { this.item = item; }

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }

        public int getLineTotal() { return lineTotal; }
        public void setLineTotal(int lineTotal) { this.lineTotal = lineTotal; }
    }

    private List<Line> lines = new ArrayList<>();
    private int itemsTotal;

    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public int getItemsTotal() { return itemsTotal; }
    public void setItemsTotal(int itemsTotal) { this.itemsTotal = itemsTotal; }
}
