package me.hhh.amonplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MathRunnable extends BukkitRunnable implements ChatListener {

    public interface ChargeListener {
        void applyCharges(int charges);
    }

    private double result;
    private String message;
    private final Player player;
    private int charges;
    private final ChargeListener chargeListener;
    private int timeOut;

    public MathRunnable(ChargeListener chargeListener, Player player) {
        this.message = "";
        this.charges = 1;
        this.chargeListener = chargeListener;
        this.timeOut = 40;
        this.player = player;
        setResult(generateEquation());
    }

    @Override
    public void run() {

        if (!("").equals(message)) {

            Integer result = null;

            try {
                result = Integer.parseInt(message);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Bitte nur ganze Zahlen");
            }

            if (result != null) {

                if (this.result == (double) result) {
                    setResult(generateEquation());
                    charges++;
                } else {
                    chargeListener.applyCharges(charges);
                    player.sendMessage(ChatColor.RED + "FALSCH");
                    this.cancel();
                }
            }
        }

        timeOut--;
        message = "";

        if(timeOut %6 == 0){
            Location loc = player.getLocation();
            loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 1);
        }

        if (timeOut <= 0) {
            chargeListener.applyCharges(charges);
            this.cancel();
        }

    }

    private void setResult(String equation) {
        this.result = ShuntingYard.evaluateExpression(equation);
        player.sendMessage(ChatColor.GOLD + equation);
    }


    @Override
    public void chatEvent(String message) {
        this.message = message;
    }

    private String getOperator(List<String> operatorList) {

        Collections.shuffle(operatorList);
        return operatorList.get(0);
    }

    private List<Integer> getDivisors(int n) {
        List<Integer> divisors = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            if (n % i == 0) {
                divisors.add(i);
            }
        }
        return divisors;
    }

    private String generateEquation() {
        StringBuilder equation = new StringBuilder();

        int threeNumbers = (int) (Math.random() * 5);

        String operator = getOperator(new ArrayList<>(Arrays.asList("+", "-", "*", "/")));

        Integer num1;
        Integer num2;

        if (("*").equals(operator)) {
            num1 = (int) (Math.random() * 21);

            if (num1 > 12) {
                num2 = num1;
            } else {
                num2 = (int) (Math.random() * 13);
            }
        } else if (("/").equals(operator)) {

            num1 = (int) (Math.random() * 101);

            List<Integer> divisors = getDivisors(num1);
            Collections.shuffle(divisors);

            num2 = divisors.get(0);

        } else {
            num1 = (int) (Math.random() * 101);
            num2 = (int) (Math.random() * 101);
        }

        equation.append(num1);
        equation.append(operator);
        equation.append(num2);

        if (threeNumbers == 4) {
            equation.append(getOperator(new ArrayList<>(Arrays.asList("+", "-"))));
            equation.append((int) (Math.random() * 21));

        }
        return equation.toString();
    }
}
