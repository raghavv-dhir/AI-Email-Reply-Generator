import { useState } from 'react';
import './App.css';
import {
  AppBar,
  Box,
  Button,
  CircularProgress,
  Container,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Snackbar,
  TextField,
  Toolbar,
  Typography,
  IconButton,
} from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { Brightness4, Brightness7, ContentCopy } from '@mui/icons-material';
import MuiAlert from '@mui/material/Alert';
import axios from 'axios';

function App() {
  const [emailContent, setEmailContent] = useState('');
  const [tone, setTone] = useState('');
  const [generatedReply, setGeneratedReply] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [darkMode, setDarkMode] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);

  const handleSubmit = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await axios.post('http://localhost:8080/api/email/generate', {
        emailContent,
        tone,
      });
      setGeneratedReply(typeof response.data === 'string' ? response.data : JSON.stringify(response.data));
    } catch (error) {
      setError('Failed to generate email reply. Please try again.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(generatedReply);
    setCopySuccess(true);
  };

  const theme = createTheme({
    palette: {
      mode: darkMode ? 'dark' : 'light',
      primary: {
        main: '#1976d2',
      },
      secondary: {
        main: '#f50057',
      },
    },
    typography: {
      fontFamily: 'Roboto, Arial, sans-serif',
      fontWeightBold: 700,
    },
  });

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ bgcolor: 'background.default', color: 'text.primary', minHeight: '100vh' }}>
        {/* Navbar */}
        <AppBar position="static" color="primary">
          <Toolbar sx={{ justifyContent: 'center', position: 'relative' }}>
            <Typography variant="h5" component="div" sx={{ fontWeight: 'bold' }}>
              📧 Email Generator
            </Typography>
            <IconButton
              color="inherit"
              onClick={() => setDarkMode(!darkMode)}
              sx={{ position: 'absolute', right: 16 }}
            >
              {darkMode ? <Brightness7 /> : <Brightness4 />}
            </IconButton>
          </Toolbar>
        </AppBar>

        {/* Main Content */}
        <Container maxWidth="md" sx={{ py: 4 }}>
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            align="center"
            sx={{ fontWeight: 'bold' }}
          >
            Generate Professional Email Replies ✨
          </Typography>

          <Box sx={{ mx: 3 }}>
            {/* Email Content Input */}
            <TextField
              fullWidth
              multiline
              rows={6}
              variant="outlined"
              label="✍️ Original Email Content"
              value={emailContent || ''}
              onChange={(e) => setEmailContent(e.target.value)}
              sx={{ mb: 3 }}
            />

            {/* Tone Selector */}
            <FormControl fullWidth sx={{ mb: 3 }}>
              <InputLabel>🎨 Tone (Optional)</InputLabel>
              <Select
                value={tone || ''}
                onChange={(e) => setTone(e.target.value)}
                label="🎨 Tone (Optional)"
              >
                <MenuItem value="">None</MenuItem>
                <MenuItem value="professional">Professional 💼</MenuItem>
                <MenuItem value="casual">Casual 😎</MenuItem>
                <MenuItem value="friendly">Friendly 😊</MenuItem>
              </Select>
            </FormControl>

            {/* Generate Button */}
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={!emailContent || loading}
              fullWidth
              sx={{
                bgcolor: !emailContent ? 'gray' : 'primary.main',
                '&:hover': {
                  bgcolor: !emailContent ? 'gray' : 'primary.dark',
                },
                color: 'white',
                py: 1.5,
                fontSize: '1rem',
                fontWeight: 'bold',
                transition: 'background-color 0.3s ease',
              }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : '🚀 Generate Reply'}
            </Button>
          </Box>

          {/* Error Message */}
          {error && (
            <Typography color="error" sx={{ mt: 3 }}>
              {error}
            </Typography>
          )}

          {/* Generated Reply */}
          {generatedReply && (
            <Box sx={{ mt: 4 }}>
              <Typography variant="h6" gutterBottom>
                ✉️ Generated Reply:
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={6}
                variant="outlined"
                value={generatedReply || ''}
                inputProps={{ readOnly: true }}
              />
              <Button
                variant="outlined"
                sx={{ mt: 2 }}
                onClick={handleCopy}
                startIcon={<ContentCopy />}
              >
                Copy to Clipboard
              </Button>
            </Box>
          )}

          {/* Snackbar for Copy Success */}
          <Snackbar
            open={copySuccess}
            autoHideDuration={3000}
            onClose={() => setCopySuccess(false)}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
          >
            <MuiAlert
              onClose={() => setCopySuccess(false)}
              severity="success"
              sx={{ width: '100%' }}
            >
              📋 Copied to clipboard!
            </MuiAlert>
          </Snackbar>
        </Container>
      </Box>
    </ThemeProvider>
  );
}

export default App;
