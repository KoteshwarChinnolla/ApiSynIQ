// pcm16-processor.js

class PCM16Processor extends AudioWorkletProcessor {
  process(inputs, outputs, parameters) {
    const input = inputs[0];
    if (input.length > 0) {
      const inputChannel = input[0]; // take first (mono) channel
      const bufferLength = inputChannel.length;
      const outputBuffer = new Int16Array(bufferLength);

      for (let i = 0; i < bufferLength; i++) {
        // clamp between -1 and 1
        let sample = inputChannel[i];
        sample = Math.max(-1, Math.min(1, sample));

        // convert to signed 16-bit
        outputBuffer[i] = sample < 0
          ? sample * 0x8000
          : sample * 0x7FFF;
      }

      // send the raw PCM16 data to main thread
      this.port.postMessage(outputBuffer);
    }

    return true;
  }
}

registerProcessor('pcm16-processor', PCM16Processor);
