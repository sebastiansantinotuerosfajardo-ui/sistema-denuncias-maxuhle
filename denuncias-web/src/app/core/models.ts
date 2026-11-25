/** Modelos que reflejan los JSON del backend (Denuncia, Seguimiento, DTO de entrada). */
export interface Denuncia {
  id: number;
  tipoDenuncia: string;
  descripcion: string;
  nombreDenunciante: string | null;
  nombreAfectado: string;
  fechaRegistro: string;
  estado: string;
  protocoloActivado: boolean;
}

export interface DenunciaRequest {
  tipoDenuncia: string;
  descripcion: string;
  nombreDenunciante: string;
  nombreAfectado: string;
}

export interface Seguimiento {
  id: number;
  denunciaId: number;
  responsable: string;
  estadoAnterior: string | null;
  estadoNuevo: string;
  observacion: string | null;
  fecha: string;
}

export interface SeguimientoRequest {
  estadoNuevo: string;
  observacion: string;
}

export type Rol = 'ADMIN' | 'OPERADOR';

/**
 * El backend guarda tipoDenuncia como texto libre (el servicio lo normaliza a mayúsculas).
 * Se usan los mismos valores que las pruebas y la colección Postman del Avance 2.
 */
export const TIPOS = ['MALTRATO_PSICOLOGICO', 'MALTRATO_FISICO', 'ACOSO_ESCOLAR', 'CONVIVENCIA'];
export const ESTADOS = ['RECIBIDA', 'PROTOCOLO_ACTIVADO', 'EN_ATENCION', 'DERIVADA', 'CERRADA'];

/** Tipos que activan el protocolo automáticamente (regla @PrePersist de la entidad Denuncia). */
export const TIPOS_CON_PROTOCOLO = ['MALTRATO_PSICOLOGICO', 'MALTRATO_FISICO', 'ACOSO_ESCOLAR'];

/** Texto legible para la interfaz; al backend siempre se envía el valor original. */
export const ETIQUETA_TIPO: Record<string, string> = {
  MALTRATO_PSICOLOGICO: 'Maltrato psicológico',
  MALTRATO_FISICO: 'Maltrato físico',
  ACOSO_ESCOLAR: 'Acoso escolar',
  CONVIVENCIA: 'Convivencia escolar',
};

export const ETIQUETA_ESTADO: Record<string, string> = {
  RECIBIDA: 'Recibida',
  PROTOCOLO_ACTIVADO: 'Protocolo activado',
  EN_ATENCION: 'En atención',
  DERIVADA: 'Derivada',
  CERRADA: 'Cerrada',
};
