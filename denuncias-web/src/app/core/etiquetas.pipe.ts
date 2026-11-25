import { Pipe, PipeTransform } from '@angular/core';
import { ETIQUETA_ESTADO, ETIQUETA_TIPO } from './models';

/** Muestra «Maltrato psicológico» en lugar de MALTRATO_PSICOLOGICO (solo presentación). */
@Pipe({ name: 'etiquetaTipo', standalone: true })
export class EtiquetaTipoPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    return valor ? (ETIQUETA_TIPO[valor] ?? valor) : '';
  }
}

/** Muestra «En atención» en lugar de EN_ATENCION (solo presentación). */
@Pipe({ name: 'etiquetaEstado', standalone: true })
export class EtiquetaEstadoPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    return valor ? (ETIQUETA_ESTADO[valor] ?? valor) : '';
  }
}
