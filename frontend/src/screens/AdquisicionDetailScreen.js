import React, { useCallback, useState } from 'react';
import { ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF' };

function money(value) {
  return Number(value || 0).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

export default function AdquisicionDetailScreen({ navigation, route }) {
  const { id } = route.params;
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const data = await clienteApi.adquisicionById(id);
      setItem(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) return <Loading text="Cargando detalle..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Compra #{item.id}</Text>
      <Text style={styles.line}>{item.producto || 'Producto sin descripcion'}</Text>
      <View style={styles.box}>
        <Text style={styles.label}>Importe</Text>
        <Text style={styles.value}>{money(item.importe)}</Text>
        <Text style={styles.label}>Comision</Text>
        <Text style={styles.value}>{money(item.comision)}</Text>
        <Text style={styles.label}>Estado</Text>
        <Text style={styles.value}>{item.estado}</Text>
      </View>

      <TouchableOpacity style={styles.primary} onPress={() => navigation.navigate('SeleccionEntrega', { id })}>
        <Text style={styles.primaryText}>Elegir entrega</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.secondary} onPress={() => navigation.navigate('EstadoEntrega', { id })}>
        <Text style={styles.secondaryText}>Ver estado de entrega</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.secondary} onPress={() => navigation.navigate('Factura', { id })}>
        <Text style={styles.secondaryText}>Ver factura</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.secondary} onPress={() => navigation.navigate('Pago', { id })}>
        <Text style={styles.secondaryText}>Pagar ahora</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: 20, paddingTop: 60, backgroundColor: palette.background },
  title: { color: palette.text, fontSize: 30, fontWeight: '900' },
  line: { color: palette.muted, marginTop: 8, marginBottom: 16 },
  box: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 18, padding: 16, marginBottom: 18 },
  label: { color: palette.muted, fontSize: 11, fontWeight: '800', textTransform: 'uppercase', marginTop: 8 },
  value: { color: palette.text, fontSize: 18, fontWeight: '900', marginTop: 2 },
  primary: { backgroundColor: palette.primary, paddingVertical: 14, borderRadius: 14, alignItems: 'center', marginBottom: 10 },
  primaryText: { color: '#fff', fontWeight: '800' },
  secondary: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, paddingVertical: 14, borderRadius: 14, alignItems: 'center', marginBottom: 10 },
  secondaryText: { color: palette.text, fontWeight: '800' },
});