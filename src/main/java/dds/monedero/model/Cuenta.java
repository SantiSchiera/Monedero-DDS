package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  private void validacionDeExtraccion(double cuanto) {
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }

    var montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());

    var limite = 1000 - montoExtraidoHoy; //huele feo

    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException(
          "No puede extraer mas de $ " + 1000 + " diarios, " + "límite: " + limite);
    }
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  private void montoPositivo(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void poner(double cuanto) {

    montoPositivo(cuanto);

    if (getMovimientos().stream()
        .filter(movimiento -> movimiento.huboMovimiento(LocalDate.now()))
        .count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    new Deposito(LocalDate.now(), cuanto).agregateA(this);
  }

  public void sacar(double cuanto) {

    montoPositivo(cuanto);

    validacionDeExtraccion(cuanto);

    new Extraccion(LocalDate.now(), cuanto).agregateA(this);
  }

  public void agregarMovimiento(Movimiento movimiento) {

    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.esDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum(); //ACA DEBE HABER
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
