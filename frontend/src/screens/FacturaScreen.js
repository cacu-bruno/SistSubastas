import React, { useCallback, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF' };

function money(value) {
  return Number(value || 0).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

export default function FacturaScreen({ route }) {
  const { id } = route.params;
  const [factura, setFactura] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const res = await clienteApi.facturaAdquisicion(id);
      setFactura(res);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) return <Loading text="Cargando factura..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Factura</Text>
      <View style={styles.card}>
        <Text style={styles.label}>Numero</Text>
        <Text style={styles.value}>{factura.numeroFactura}</Text>
        <Text style={styles.label}>Importe</Text>
        <Text style={styles.value}>{money(factura.importe)}</Text>
        <Text style={styles.label}>Comision</Text>
        <Text style={styles.value}>{money(factura.comision)}</Text>
        <Text style={styles.label}>Costo envio</Text>
        <Text style={styles.value}>{money(factura.costoEnvio)}</Text>
        <Text style={styles.label}>Total</Text>
        <Text style={styles.value}>{money(factura.total)}</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingTop: 60, backgroundColor: palette.background },
  title: { color: palette.text, fontSize: 30, fontWeight: '900', marginBottom: 16 },
  card: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 18, padding: 16 },
  label: { color: palette.muted, fontSize: 11, fontWeight: '800', textTransform: 'uppercase', marginTop: 8 },
  value: { color: palette.text, fontSize: 18, fontWeight: '900', marginTop: 4 },
});