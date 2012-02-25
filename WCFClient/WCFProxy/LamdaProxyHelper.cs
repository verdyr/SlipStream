using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel;
using System.Threading;

namespace WCFClient
{

    /// <summary>
    /// This delegate describes the method on the interface to be called.
    /// </summary>
    /// <typeparam name="T">This is the type of the interface</typeparam>
    /// <param name="proxy">This is the method.</param>
    public delegate void UseServiceDelegate<T>(T proxy);

    /// <summary>
    /// This delegate describes the method on the interface to be called.
    /// </summary>
    /// <typeparam name="T">This is the type of the interface</typeparam>
    /// <param name="proxy">This is the method.</param>
    /// <param name="obj">This is any object which may be used to identify execution instance.</param>
    public delegate void UseServiceDelegateWithAsyncReturn<T>(T proxy, object obj);
    /// <summary>
    /// Helper class for creating proxies at the client end for the exposed services.
    /// Usage:  ProxyHelper<IService>.Use(serviceProxy =>
    ///        {
    ///            returnObject = serviceProxy.SomeMethod(parameters);
    ///        },ChannelName);
    /// </summary>
    /// <typeparam name="T">This is the type of the interface.</typeparam>
    public  class LamdaProxyHelper<T>
    {
        /// <summary>
        /// This is the channel proxy
        /// </summary>
        IClientChannel proxy = null;
        /// <summary>
        /// This is the callback method for an async call back.
        /// </summary>
        AsyncCallback callBack = null;
        /// <summary>
        /// This is the method to be executed.
        /// </summary>
        UseServiceDelegate<T> codeBlock = null;

        /// <summary>
        /// This is the store of the channel.
        /// </summary>
        private static IDictionary<string, ChannelFactory<T>> channelPool
            = new Dictionary<string, ChannelFactory<T>>();


        UseServiceDelegateWithAsyncReturn<T> codeBlockWithAsyncReturn = null;
        /// <summary>
        /// Returns an instance of the channel object. The channel is not yet open.
        /// </summary>
        /// <param name="WCFEndPoint">This is the end point</param>
        /// <returns>Return List of all the invoked proxies</returns>
        private  ChannelFactory<T> GetChannelFactory(string WCFEndPoint)
        {
            ChannelFactory<T> channelFactory = null;
            //Check if the channel factory exists
            //Create and return an instance of the channel            
            if (! channelPool.TryGetValue(WCFEndPoint,out channelFactory))
            {
                channelFactory = new ChannelFactory<T>(WCFEndPoint);
                channelPool.Add(WCFEndPoint, channelFactory);
            }
            return channelFactory;
        }

        /// <summary>
        /// Invokes the method on the WCF interface with the given end point to 
        /// create a channel
        /// Usage
        /// new ProxyHelper<InterfaceName>().Use(serviceProxy =>
        ///         {
        ///             value = serviceProxy.MethodName(params....);
        ///         }, "WCFEndPoint");
        /// </summary>
        /// <param name="codeBlock">The WCF interface method of interface of type T
        /// </param>
        /// <param name="WCFEndPoint">The end point.</param>
        public  void Use(UseServiceDelegate<T> codeBlock, string WCFEndPoint)
        {            
            try
            {
                //Create an instance of proxy
                this.proxy = GetChannelFactory(WCFEndPoint).CreateChannel() as IClientChannel;
                if (this.proxy != null)
                {
                    //open the proxy
                    this.proxy.Open();
                    //Call the method
                    codeBlock((T)this.proxy);
                    
                    this.proxy.Close();
                }
            }
            catch (CommunicationException communicationException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw communicationException;
            }
            catch (TimeoutException timeoutException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw timeoutException;
            }
            catch (Exception ex)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw ex;
            }
        }

        /// <summary>
        /// This method is called when the proxy is called using an
        /// async method
        /// </summary>
        /// <param name="ar">The result</param>
        private void AsyncResult(IAsyncResult ar)
        {
            //end the invocation
            this.codeBlock.EndInvoke(ar);      
            //close the proxy
            this.proxy.Close();
            //callback the method
            this.callBack(ar);
        }

        /// <summary>
        /// Invokes the method on the WCF interface with the given end point to 
        /// create a channel
        /// Usage
        /// new ProxyHelper<InterfaceName>().Use(serviceProxy =>
        ///         {
        ///             value = serviceProxy.MethodName(params....);
        ///         }, "WCFEndPoint",callBackMethodName,id);
        /// </summary>
        /// <param name="codeBlock">The WCF interface method of interface of type T
        /// </param>
        /// <param name="WCFEndPoint">The end point.</param>
        /// <param name="obj">The object instance used to identify in callback</param>
        public  void UseAsync(UseServiceDelegate<T> codeBlock, string WCFEndPoint,AsyncCallback callBack,object obj)
        {
            try
            {
                this.proxy = GetChannelFactory(WCFEndPoint).CreateChannel() as IClientChannel;
                if (this.proxy != null)
                {
                    
                    this.proxy.Open();
                    this.callBack = callBack;
                    this.codeBlock = codeBlock;                    
                    IAsyncResult result =  codeBlock.BeginInvoke((T)this.proxy, AsyncResult, obj);                                                   
                }
            }
            catch (CommunicationException communicationException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw communicationException;
            }
            catch (TimeoutException timeoutException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw timeoutException;
            }
            catch (Exception ex)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw ex;
            }
        }
        
        /// <summary>
        /// This method calls the WCF Service in a new thread. The calling of other method for result is the 
        /// responcibility of the client code
        /// </summary>
        /// <param name="codeBlock">The method on the WCF service to be called</param>
        /// <param name="WCFEndPoint">This is the WCF end point</param>
        /// <param name="obj">This is any object which may help in exeution of the async parameters</param>
        public void UseAsyncWithReturnValue(UseServiceDelegateWithAsyncReturn<T> codeBlock, string WCFEndPoint, object obj)
        {
            try
            {
                this.proxy = GetChannelFactory(WCFEndPoint).CreateChannel() as IClientChannel;
                if (this.proxy != null)
                {                                        
                    this.codeBlockWithAsyncReturn = codeBlock;
                    new Thread(() =>
                    {   //Create a new thread and on the new thread call the methos
                        codeBlock((T)this.proxy,obj);
                        this.proxy.Close();
                    }).Start();
                    
                }
            }
            catch (CommunicationException communicationException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw communicationException;
            }
            catch (TimeoutException timeoutException)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw timeoutException;
            }
            catch (Exception ex)
            {
                if (this.proxy != null)
                {
                    this.proxy.Abort();
                }
                throw ex;
            }

        }
    }
}
