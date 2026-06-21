import React, { useCallback, useState } from 'react';
import { Alert, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF' };

function money(value) {
  return Number(value || 0).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

export default function PagoScreen({ route }) {
  const { id, kind = 'adquisicion' } = route.params;
  const [detalle, setDetalle] = useState(null);
  const [medios, setMedios] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const [detallePago, paymentMethods] = await Promise.all([
        kind === 'multa' ? clienteApi.multaById(id) : clienteApi.adquisicionById(id),
        clienteApi.metodosPago().catch(() => []),
      ]);
      setDetalle(detallePago);
      setMedios(paymentMethods || []);
      setSelected(paymentMethods?.[0]?.id || null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  async function pagar() {
    try {
      if (!selected) {
        Alert.alert('Selecciona un medio de pago', 'Necesitas elegir un medio de pago verificado.');
        return;
      }
      if (kind === 'multa') {
        await clienteApi.pagarMulta(id, selected);
        Alert.alert('Pago realizado', 'La multa fue pagada correctamente.');
      } else {
        await clienteApi.pagarAdquisicion(id, selected);
        Alert.alert('Pago realizado', 'La adquisicion fue pagada correctamente.');
      }
    } catch (err) {
      Alert.alert('No se pudo pagar', err.message || 'Ocurrio un error');
    }
  }

  if (loading) return <Loading text="Cargando pago..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Pago</Text>
      <View style={styles.card}>
        <Text style={styles.label}>{kind === 'multa' ? 'Multa' : 'Compra'}</Text>
        <Text style={styles.value}>#{detalle.id}</Text>
        <Text style={styles.label}>Total estimado</Text>
        <Text style={styles.value}>{money(kind === 'multa' ? detalle.importe : (detalle.importe || 0) + (detalle.comision || 0))}</Text>
      </View>

      <Text style={styles.sectionTitle}>Medios de pago</Text>
      {medios.map((medio) => (
        <TouchableOpacity key={medio.id} style={[styles.method, selected === medio.id && styles.methodSelected]} onPress={() => setSelected(medio.id)}>
          <Text style={styles.methodTitle}>{medio.marca || medio.tipo}</Text>
          <Text style={styles.methodSub}>{medio.estado}</Text>
        </TouchableOpacity>
      ))}

      <TouchableOpacity style={styles.primary} onPress={pagar}><Text style={styles.primaryText}>Pagar adquisicion</Text></TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingTop: 60, backgroundColor: palette.background },
  title: { color: palette.text, fontSize: 30, fontWeight: '900', marginBottom: 16 },
  card: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 18, padding: 16, marginBottom: 18 },
  label: { color: palette.muted, fontSize: 11, fontWeight: '800', textTransform: 'uppercase', marginTop: 8 },
  value: { color: palette.text, fontSize: 18, fontWeight: '900', marginTop: 4 },
  sectionTitle: { color: palette.text, fontSize: 18, fontWeight: '900', marginBottom: 10 },
  method: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 14, padding: 14, marginBottom: 10 },
  methodSelected: { borderColor: palette.primary, borderWidth: 2 },
  methodTitle: { color: palette.text, fontWeight: '900' },
  methodSub: { color: palette.muted, marginTop: 4 },
  primary: { backgroundColor: palette.primary, paddingVertical: 14, borderRadius: 14, alignItems: 'center', marginTop: 8 },
  primaryText: { color: '#fff', fontWeight: '800' },
});