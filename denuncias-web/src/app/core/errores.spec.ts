import { HttpErrorResponse } from '@angular/common/http';
import { mensajeError } from './errores';
import { EtiquetaEstadoPipe, EtiquetaTipoPipe } from './etiquetas.pipe';

describe('mensajeError (contrato de errores del backend)', () => {
  const err = (status: number, error?: unknown) => new HttpErrorResponse({ status, error });

  it('status 0 → indica que no hay conexión con la API', () => {
    expect(mensajeError(err(0))).toContain('No hay conexión con la API');
  });

  it('401 → credenciales inválidas', () => {
    expect(mensajeError(err(401))).toBe('Credenciales inválidas (401)');
  });

  it('403 → sin permiso', () => {
    expect(mensajeError(err(403))).toBe('No tiene permiso para esta operación (403)');
  });

  it('409 → muestra el mensaje de la regla de negocio que envía el backend', () => {
    const e = err(409, { error: 'La denuncia ya está cerrada y no admite cambios de estado' });
    expect(mensajeError(e)).toBe('La denuncia ya está cerrada y no admite cambios de estado (409)');
  });

  it('400 → agrega el detalle de los campos inválidos', () => {
    const e = err(400, { error: 'Datos de entrada no válidos', campos: { descripcion: 'La descripción es obligatoria' } });
    expect(mensajeError(e)).toBe('Datos de entrada no válidos (400) — La descripción es obligatoria');
  });

  it('respuesta sin JSON → mensaje genérico con el código', () => {
    expect(mensajeError(err(500))).toBe('Error (500)');
  });
});

describe('pipes de etiquetas', () => {
  it('traducen valores conocidos y dejan pasar los desconocidos', () => {
    const tipo = new EtiquetaTipoPipe();
    const estado = new EtiquetaEstadoPipe();
    expect(tipo.transform('MALTRATO_PSICOLOGICO')).toBe('Maltrato psicológico');
    expect(tipo.transform('OTRO_TIPO')).toBe('OTRO_TIPO');
    expect(estado.transform('PROTOCOLO_ACTIVADO')).toBe('Protocolo activado');
    expect(estado.transform(null)).toBe('');
  });
});
