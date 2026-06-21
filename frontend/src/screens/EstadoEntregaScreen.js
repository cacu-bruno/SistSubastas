import React, { useCallback, useState } from 'react';
import { Alert, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF' };

export default function EstadoEntregaScreen({ route }) {
  const { id } = route.params;
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const res = await clienteApi.entregaByAdquisicion(id);
      setData(res);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  async function confirmar() {
    try {
      await clienteApi.confirmarRecepcion(id);
      await load();
    } catch (err) {
      Alert.alert('No se pudo confirmar', err.message || 'Ocurrio un error');
    }
  }

  if (loading) return <Loading text="Cargando entrega..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Estado de entrega</Text>
      <View style={styles.card}>
        <Text style={styles.label}>Tipo</Text>
        <Text style={styles.value}>{data.tipo}</Text>
        <Text style={styles.label}>Estado</Text>
        <Text style={styles.value}>{data.estado}</Text>
        <Text style={styles.label}>Direccion</Text>
        <Text style={styles.value}>{data.direccion || 'No aplica'}</Text>
        <Text style={styles.label}>Codigo retiro</Text>
        <Text style={styles.value}>{data.codigoRetiro || 'No aplica'}</Text>
        <Text style={styles.label}>Seguimiento</Text>
        <Text style={styles.value}>{data.codigoSeguimiento || 'No aplica'}</Text>
      </View>
      <TouchableOpacity style={styles.primary} onPress={confirmar}><Text style={styles.primaryText}>Confirmar recepcion</Text></TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingTop: 60, backgroundColor: palette.background },
  title: { color: palette.text, fontSize: 30, fontWeight: '900', marginBottom: 16 },
  card: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 18, padding: 16, marginBottom: 18 },
  label: { color: palette.muted, fontSize: 11, fontWeight: '800', textTransform: 'uppercase', marginTop: 8 },
  value: { color: palette.text, fontSize: 16, fontWeight: '800', marginTop: 4 },
  primary: { backgroundColor: palette.primary, paddingVertical: 14, borderRadius: 14, alignItems: 'center' },
  primaryText: { color: '#fff', fontWeight: '800' },
});